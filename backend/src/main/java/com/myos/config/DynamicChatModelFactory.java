package com.myos.config;

import com.myos.entity.AiModelConfig;
import com.myos.service.AiModelConfigService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @Component registers this class as a Spring Bean managed in the container.
 *
 * @Primary is a crucial Spring Boot annotation. It solves bean ambiguity.
 * If Spring's classpath has multiple ChatModel beans (Ollama, OpenAI, Anthropic),
 * @Primary tells Spring: "Always inject THIS DynamicChatModelFactory delegator by default."
 *
 * Design Pattern: **Proxy Pattern** (or Delegator Pattern).
 * Instead of autowiring a static connection, our services wire this single delegator.
 * When {@code call(prompt)} is executed, the proxy dynamically:
 * 1. Checks PostgreSQL for the active LLM.
 * 2. Instantiates the correct Spring AI model client at runtime.
 * 3. Decrypts credentials.
 * 4. Transparently routes prompt executions.
 *
 * To prevent excessive object creation on every prompt call, it implements a lazy caching mechanism
 * that only rebuilds the target LLM client when the database active configuration is updated.
 */
@Component
@Primary
public class DynamicChatModelFactory implements ChatModel {

    private final AiModelConfigService configService;

    // Cache parameters to hold instantiated client until model config changes
    private ChatModel cachedClient;
    private UUID cachedConfigId;
    private String cachedModelName;

    public DynamicChatModelFactory(AiModelConfigService configService) {
        this.configService = configService;
    }

    /**
     * Resolves the active underlying model client, checking the cache first.
     */
    private synchronized ChatModel getActiveClient() {
        AiModelConfig activeConfig = configService.getActiveConfig();

        // If no client is cached yet, or the active model choice has been updated, rebuild!
        if (cachedClient == null || !activeConfig.getId().equals(cachedConfigId) || !activeConfig.getModelName().equals(cachedModelName)) {
            this.cachedClient = buildClient(activeConfig);
            this.cachedConfigId = activeConfig.getId();
            this.cachedModelName = activeConfig.getModelName();
        }

        return cachedClient;
    }

    /**
     * Runtime Builder: Manually instantiates low-level API clients based on DB configurations.
     */
    private ChatModel buildClient(AiModelConfig config) {
        String provider = config.getProvider().toUpperCase();
        return switch (provider) {
            case "OLLAMA" -> {
                // Initialize local Ollama connection
                OllamaApi api = new OllamaApi(config.getBaseUrl());
                yield new OllamaChatModel(api);
            }
            case "OPENAI" -> {
                // Decrypt cloud key, and initialize OpenAI connection
                String decryptedKey = configService.getDecryptedApiKey(config.getId());
                OpenAiApi api = new OpenAiApi(decryptedKey);
                yield new OpenAiChatModel(api);
            }
            case "ANTHROPIC" -> {
                // Decrypt cloud key, and initialize Anthropic Claude connection
                String decryptedKey = configService.getDecryptedApiKey(config.getId());
                AnthropicApi api = new AnthropicApi(decryptedKey);
                yield new AnthropicChatModel(api);
            }
            default -> throw new IllegalArgumentException("Unsupported AI Provider: " + provider);
        };
    }

    /**
     * Intercepts standard ChatModel call.
     * Routes it dynamically to the active underlying client in a thread-safe manner.
     */
    @Override
    public ChatResponse call(Prompt prompt) {
        return getActiveClient().call(prompt);
    }

    /**
     * Overrides getDefaultOptions as required by Spring AI ChatModel interface.
     * Delegates to the active client's options dynamically.
     */
    @Override
    public org.springframework.ai.chat.prompt.ChatOptions getDefaultOptions() {
        return getActiveClient().getDefaultOptions();
    }
}
