package com.goblin.aicodegenerator.service.impl;

import com.goblin.aicodegenerator.innerService.InnerScreenshotService;
import com.goblin.aicodegenerator.service.ScreenshotService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @Author goblin
 * @Date 2025/8/27 21:35
 * @注释
 */
@DubboService
public class InnerScreenshotServiceImpl implements InnerScreenshotService {

    @Resource
    private ScreenshotService screenshotService;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        return screenshotService.generateAndUploadScreenshot(webUrl);
    }
}
