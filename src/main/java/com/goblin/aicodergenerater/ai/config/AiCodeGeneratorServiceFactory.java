package com.goblin.aicodergenerater.ai.config;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.goblin.aicodergenerater.ai.AiCodeGeneratorService;
import com.goblin.aicodergenerater.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.beans.ConstructorProperties;
import java.time.Duration;

/**
 * AI service 工厂
 * 根据 appId 创建不同的 AI 服务实例
 */
@Slf4j
@Configuration
//@Data
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

//    private final RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices
//                .builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(streamingChatModel)
//                .chatMemoryProvider(memoryId ->
//                     MessageWindowChatMemory
//                            .builder()
//                            .id(memoryId)
//                            .chatMemoryStore(redisChatMemoryStore)
//                            .maxMessages(20)
//                            .build()
//                )
//                .build();
//    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }

    /**
     * AI 服务实例缓存
     * 缓存策略：
     *  - 最大缓存 1000 个实例
     *  - 写入后 30 分钟过期
     *  - 访问后 10 分钟过期
     */
    private final Cache<Long ,AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被溢出，appId：{}，原因：{}",key,cause);
            })
            .build();

    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId){
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId) {
        log.info("为 appId：{} 创建新的 AI 服务实例",appId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        chatHistoryService.loadChatHistoryToMemory(appId,chatMemory,20);
        AiCodeGeneratorService aiCodeGeneratorService = AiServices
                .builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
        return aiCodeGeneratorService;
    }
}
