package com.goblin.aicodergenerater.cos;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author goblin
 * @Date 2025/8/13 16:13
 * @注释
 */
@SpringBootTest
@Slf4j
class S3ManagerTest {

    @Resource
    private S3Manager s3Manager;

    @Resource
    private S3ClientConfig s3ClientConfig;
    @Test
    void putObject() {
        File file = new File("");
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = String.format("screenshots/%s/%s", datePath, file.getName());
        PutObjectResponse putObjectResponse = s3Manager.putObject(key, file);
        if (putObjectResponse != null) {
            // 构建访问URL
            String url = String.format("%s/%s/%s", s3ClientConfig.getHost(),s3ClientConfig.getBucket(), key);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
        } else {
            log.error("文件上传COS失败，返回结果为空");
        }
    }
}