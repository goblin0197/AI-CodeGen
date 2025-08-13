package com.goblin.aicodergenerater.service;

/**
 * @Author goblin
 * @Date 2025/8/13 13:09
 * @注释
 */
public interface ScreenshotService {
    /**
     * 生成截图并上传
     * @param webUrl
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);
}
