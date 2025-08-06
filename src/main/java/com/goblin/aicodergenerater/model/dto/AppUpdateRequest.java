package com.goblin.aicodergenerater.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用更新请求
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;
//
//    /**
//     * 应用封面
//     */
//    private String cover;
//
//    /**
//     * 应用初始化的 prompt
//     */
//    private String initPrompt;
//
//    /**
//     * 代码生成类型（枚举）
//     */
//    private String codeGenType;
//
//    /**
//     * 优先级
//     */
//    private Integer priority;

    private static final long serialVersionUID = 1L;
}