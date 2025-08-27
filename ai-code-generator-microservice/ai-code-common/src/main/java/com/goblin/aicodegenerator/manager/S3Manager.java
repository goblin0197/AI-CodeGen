package com.goblin.aicodegenerator.manager;

import com.goblin.aicodegenerator.config.S3ClientConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;

/**
 * @Author goblin
 * @Date 2025/8/13 12:56
 * @注释
 */
@Component
@ConditionalOnBean(S3ClientConfig.class)
@Slf4j
public class S3Manager {
    @Resource
    private S3Client s3Client;

    @Resource
    private S3ClientConfig s3ClientConfig;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResponse putObject(String key, File file) {
        return s3Client.putObject(
                PutObjectRequest.builder().bucket(s3ClientConfig.getBucket()).key(key).build(),
                RequestBody.fromFile(file)
        );
    }

    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        // 上传文件
        PutObjectResponse result = putObject(key, file);
        if (result != null) {
            // 构建访问URL
            String url = String.format("%s/%s/%s", s3ClientConfig.getHost(),s3ClientConfig.getBucket(), key);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传COS失败，返回结果为空");
            return null;
        }
    }
}
