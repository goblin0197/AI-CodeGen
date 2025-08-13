package com.goblin.aicodergenerater.cos;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * @Author goblin
 * @Date 2025/8/13 12:47
 * @注释
 */
@Configuration
@ConfigurationProperties(prefix = "s3.client")
@Data
public class S3ClientConfig {

    private String host;
    private String accessKey;
    private String secretKey;
    private String bucket;

    @Bean
    public S3Client S3Client(){
        // 1. 初始化 S3 客户端
        return S3Client.builder()
                .endpointOverride(URI.create(host)) // RustFS 地址
                .region(Region.US_EAST_1) // 可写死，RustFS 不校验 region
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .forcePathStyle(true) // 关键配置！RustFS 需启用 Path-Style
                .build();
    }

}
