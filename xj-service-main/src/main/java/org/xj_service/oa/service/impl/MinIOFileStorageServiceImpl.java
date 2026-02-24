package org.xj_service.oa.service.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.service.FileStorageService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储实现（支持动态建桶）
 */
@Slf4j
@Service("minioFileStorageService") // 指定Bean名称，方便通过配置切换
public class MinIOFileStorageServiceImpl implements FileStorageService {

    // MinIO配置
    @Value("${file.storage.minio.endpoint}")
    private String endpoint;

    @Value("${file.storage.minio.access-key}")
    private String accessKey;

    @Value("${file.storage.minio.secret-key}")
    private String secretKey;

    // 默认桶（未指定桶时使用）
    @Value("${file.storage.minio.default-bucket:default-files}")
    private String defaultBucket;


    private MinioClient minioClient;

    // 最小IO缓冲区（5MB）
    private static final int BUFFER_SIZE = 5 * 1024 * 1024;

    // 初始化MinIO客户端
    public MinIOFileStorageServiceImpl() {
        // 构造方法中初始化客户端（或使用@PostConstruct）
    }

    // 懒加载客户端（确保配置已注入）
    @PostConstruct
    private MinioClient getMinioClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            // 确保默认桶存在
            createBucket(defaultBucket);
        }
        return minioClient;
    }
    @Override
    public String upload(MultipartFile file, String bucket, String bizType) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        try {
            // 1. 确定桶名（为空则用默认桶）
            String targetBucket = bucket == null ? defaultBucket : bucket;
            createBucket(targetBucket); // 自动创建桶

            // 2. 生成文件路径（业务类型+日期+唯一文件名）
            String originalName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + cleanFileName(originalName);
            String dateDir = java.time.LocalDate.now().toString().replace("-", "");
            String objectName = String.format("%s/%s/%s", bizType, dateDir, fileName);

            // 3. 上传文件
            getMinioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String fullPath = targetBucket + "/" + objectName;
            log.info("文件上传成功：{}", fullPath);
            return fullPath;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }
    /**
     * 上传文件到指定存储桶
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType 文件内容类型
     * @param bucket 存储桶名称
     * @param bizType 业务类型，用于生成文件存储路径
     * @return 文件在存储系统中的完整路径，格式为"bucket/bizType/日期/文件名"
     * @throws RuntimeException 当文件上传过程中发生异常时抛出
     */
    @Override
    public String upload(InputStream inputStream, String fileName, String contentType, String bucket, String bizType) {
        try {
            // 2. 确保桶存在
            createBucketIfNotExists(bucket);

            // 3. 生成统一路径：bizType/日期/文件名（如leave/20251030/123456_test.pdf）
            String objectName = generateObjectName(fileName, bizType);

            // 4. 上传流（使用MinIO分块上传，避免内存溢出）
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, -1, BUFFER_SIZE) // 分块大小1MB
                            .contentType(contentType)
                            .build()
            );

            return bucket + "/" + objectName; // 返回完整路径
        } catch (Exception e) {
            log.error("文件上传失败：{}", fileName, e);
            throw new RuntimeException("文件上传失败");
        } finally {
            // 关闭输入流（避免资源泄漏）
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("流关闭失败", e);
            }
        }
    }
    @Override
    public InputStream download(String filePath) {
        try {
            String[] parts = splitFilePath(filePath);
            return getMinioClient().getObject(
                    GetObjectArgs.builder()
                            .bucket(parts[0])
                            .object(parts[1])
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败：{}", filePath, e);
            throw new RuntimeException("文件下载失败");
        }
    }
    @Override
    public void delete(String filePath) {
        try {
            String[] parts = splitFilePath(filePath);
            getMinioClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(parts[0])
                            .object(parts[1])
                            .build()
            );
            log.info("文件删除成功：{}", filePath);
        } catch (Exception e) {
            log.error("文件删除失败：{}", filePath, e);
            throw new RuntimeException("文件删除失败");
        }
    }
    @Override
    public String getTempUrl(String filePath, Integer expireSeconds) {
        try {
            String[] parts = splitFilePath(filePath);
            int expires = expireSeconds == null ? 3600 : expireSeconds; // 默认1小时
            return getMinioClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(parts[0])
                            .object(parts[1])
                            .expiry(expires, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成临时URL失败：{}", filePath, e);
            throw new RuntimeException("获取文件访问链接失败");
        }
    }
    @Override
    public void createBucket(String bucket) {
        try {
            MinioClient client = getMinioClient();
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("存储桶创建成功：{}", bucket);
            }
        } catch (Exception e) {
            log.error("存储桶创建失败：{}", bucket, e);
            throw new RuntimeException("创建存储桶失败：" + bucket);
        }
    }

    // 辅助方法：拆分路径为桶和对象名
    private String[] splitFilePath(String filePath) {
        int firstSlash = filePath.indexOf("/");
        if (firstSlash == -1) {
            throw new IllegalArgumentException("无效文件路径：" + filePath + "（格式应为bucket/object）");
        }
        return new String[]{
                filePath.substring(0, firstSlash),
                filePath.substring(firstSlash + 1)
        };
    }

    // 清理文件名（去除特殊字符）
    private String cleanFileName(String fileName) {
        if (fileName == null) return "unknown";
        // 允许字母（包括中文）、数字、下划线、点号和连字符
        return fileName.replaceAll("[^\\p{L}\\p{N}_.-]", "_");
    }

    // 创建桶（如果不存在）
    private void createBucketIfNotExists(String bucketName) throws Exception {
        // 添加参数空值检查
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("存储桶名称不能为空");
        }

        // 添加MinIO客户端空值检查
        if (minioClient == null) {
            throw new IllegalStateException("MinIO客户端未初始化");
        }

        try {
            // 原有的创建存储桶逻辑
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("创建存储桶失败: " + e.getMessage(), e);
        }
    }

    // 生成统一的对象名（业务类型+日期+清洗后的文件名）
    private String generateObjectName(String fileName, String bizType) {
        String cleanName = fileName.replaceAll("[^a-zA-Z0-9_.-]", "_"); // 清洗特殊字符
        String dateDir = LocalDate.now().toString().replace("-", ""); // 日期目录（20251030）
        return String.format("%s/%s/%s", bizType, dateDir, cleanName);
    }
}
