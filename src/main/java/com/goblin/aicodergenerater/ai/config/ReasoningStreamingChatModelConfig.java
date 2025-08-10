package com.goblin.aicodergenerater.ai.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author goblin
 * @Date 2025/8/10 17:23
 * @注释
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
public class ReasoningStreamingChatModelConfig {
    private String baseUrl;
    private String apiKey;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 为了测试方便临时修改
        final String modelName = "deepseek-ai/DeepSeek-V3";
        final int maxTokens = 8192;
        // 生产环境使用：
        // final String modelName = "deepseek-ai/DeepSeek-R1-0528";
        // final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
