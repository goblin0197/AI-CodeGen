package com.goblin.aicodergenerater.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author goblin
 * @Date 2025/8/12 20:45
 * @注释
 */
@Slf4j
@SpringBootTest
class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String url = "https://www.codefather.cn";
        String result = WebScreenshotUtils.saveWebPageScreenshot(url);
        Assertions.assertNotNull(result);
    }
}