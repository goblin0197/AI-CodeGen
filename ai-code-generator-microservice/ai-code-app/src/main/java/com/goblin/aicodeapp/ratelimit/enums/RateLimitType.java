package com.goblin.aicodeapp.ratelimit.enums;

/**
 * @Author goblin
 * @Date 2025/8/15 23:31
 * @注释
 */
public enum RateLimitType {

    /**
     * 接口级别限流
     */
    API,

    /**
     * 用户级别限流
     */
    USER,

    /**
     * IP级别限流
     */
    IP
}

