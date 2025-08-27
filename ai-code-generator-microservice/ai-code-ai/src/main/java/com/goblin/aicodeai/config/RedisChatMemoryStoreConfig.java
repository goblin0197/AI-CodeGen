package com.goblin.aicodeai.config;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redis 存储对话记忆 配置类
 * 默认存入 0 号数据库
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    private String host;

    private int port;

    private String password;

    private String user;

    private long ttl;

    private int database;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .password(password)
                .ttl(ttl);
        if(StrUtil.isNotBlank(password)){
            builder.user(StrUtil.isNotBlank(user) ? user : "default");
        }
        return builder.build();
    }
}
