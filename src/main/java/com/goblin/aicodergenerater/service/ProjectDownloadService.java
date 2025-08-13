package com.goblin.aicodergenerater.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author goblin
 * @Date 2025/8/13 17:01
 * @注释
 */
public interface ProjectDownloadService {
    /**
     * 下载项目为zip压缩包
     * @param projectPath
     * @param downloadFileName
     * @param response
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
