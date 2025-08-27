package com.goblin.aicodegenerator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Resource;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @Author goblin
 * @Date 2025/8/15 21:54
 * @注释
 */
@Configuration
public class RedisCacheManageConfig {
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public CacheManager cacheManager(){
        // 配置 ObjectMapper 支持Java8时间类型 LocalDateTime
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 启用默认类型信息（针对非final类），以便反序列化时能恢复原类型
        // value 使用 JSON 序列化器时需要开启以下配置
//        objectMapper.activateDefaultTyping(
//                objectMapper.getPolymorphicTypeValidator(),
//                ObjectMapper.DefaultTyping.NON_FINAL
//        );

        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                // key 使用 String 序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
                // value 使用 JSON 序列化器（支持复杂对象）
                // 如果对 value 进行 JSON 序列化，可能会出现无法反序列化的情况，因为 Redis 中并没有存储 Java 类的信息，不知道要反序列化成哪个类，就会报错。
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                // 针对 精选应用 缓存配置5分钟过期
                .withCacheConfiguration("good_app_page",defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}
