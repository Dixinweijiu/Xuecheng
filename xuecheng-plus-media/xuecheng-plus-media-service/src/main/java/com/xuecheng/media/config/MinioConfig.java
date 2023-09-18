package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/17 20:01
 */
@Configuration
public class MinioConfig {

    //minio终端地址
    @Value("${minio.endpoint}")
    private String endpoint;
    //minio账户名
    @Value("${minio.accessKey}")
    private String accessKey;
    //minio账户密码
    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        return minioClient;
    }
}
