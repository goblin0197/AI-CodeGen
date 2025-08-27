package com.goblin.aicodeclient.innerService;

/**
 * 内部截图服务
 */
public interface innerScreenshotService {
    /**
     * 生成截图并上传
     * @param webUrl
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);
}
