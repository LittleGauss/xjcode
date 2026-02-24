package org.xj_service.oa.service;

import io.minio.PutObjectArgs;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 通用文件存储接口（可扩展支持多种存储方案）
 */
public interface FileStorageService {

    /**
     * 上传文件
     * @param file      上传文件
     * @param bucket    存储桶名（可为null，使用默认桶）
     * @param bizType   业务类型（用于分目录，如"leave"、"contract"）
     * @return 存储路径（格式：bucket/bizType/xxx）
     */
    String upload(MultipartFile file, String bucket, String bizType);

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
    public String upload(InputStream inputStream, String fileName, String contentType, String bucket, String bizType) ;


    /**
     * 下载文件
     * @param filePath 存储路径（格式：bucket/xxx）
     * @return 文件输入流
     */
    InputStream download(String filePath);

    /**
     * 删除文件
     * @param filePath 存储路径（格式：bucket/xxx）
     */
    void delete(String filePath);

    /**
     * 获取文件临时访问URL
     * @param filePath 存储路径
     * @param expireSeconds 过期时间（秒）
     * @return 临时URL
     */
    String getTempUrl(String filePath, Integer expireSeconds);

    /**
     * 动态创建存储桶
     * @param bucket 桶名
     */
    void createBucket(String bucket);
}