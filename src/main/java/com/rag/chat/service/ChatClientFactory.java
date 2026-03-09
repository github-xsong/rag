package com.rag.chat.service;

import com.rag.model.entity.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatClientFactory {

    private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();

    public ChatClient createChatClient(ModelConfig config) {
        String cacheKey = buildCacheKey(config);

        return clientCache.computeIfAbsent(cacheKey, key -> {
            log.info("创建新的 ChatClient: {} ({})", config.getName(), config.getModelName());
            return buildChatClient(config);
        });
    }

    private ChatClient buildChatClient(ModelConfig config) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.getModelName())
                .temperature(0.7)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        return ChatClient.builder(chatModel).build();
    }

    private String buildCacheKey(ModelConfig config) {
        return config.getId() + ":" + config.getModelName() + ":" + config.getBaseUrl();
    }

    public void clearCache() {
        clientCache.clear();
        log.info("ChatClient 缓存已清空");
    }

    public void removeFromCache(Long configId) {
        clientCache.entrySet().removeIf(entry -> entry.getKey().startsWith(configId + ":"));
        log.info("已移除模型配置 {} 的缓存", configId);
    }
}
