package org.xj_service.oa.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.impl.MinIOFileStorageServiceImpl;

/**
 * 文件存储配置（通过配置选择存储实现）
 */
@Configuration
public class FileStorageConfig {

    // 当配置文件中 file.storage.type=minio 时，启用MinIO实现
    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "minio", matchIfMissing = true)
    public FileStorageService fileStorageService() {
        return new MinIOFileStorageServiceImpl();
    }

    // 未来可添加其他存储实现（如本地存储）
    // @Bean
    // @ConditionalOnProperty(name = "file.storage.type", havingValue = "local")
    // public FileStorageService localFileStorageService() {
    //     return new LocalFileStorageServiceImpl();
    // }
}