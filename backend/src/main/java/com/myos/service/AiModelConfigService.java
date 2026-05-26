package com.myos.service;

import com.myos.entity.AiModelConfig;
import com.myos.repository.AiModelConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.myos.exception.ErrorCode;
import com.myos.exception.MyOsException;

import java.util.List;
import java.util.UUID;

/**
 * @Service registers this class as a business service bean in the Spring container.
 * In a standard Spring Boot layered architecture, all business rules and orchestration must
 * live in the Service layer, keeping Controller and Repository layers focused on their primary duties.
 *
 * @Transactional coordinates data transactions. It wraps service operations inside database
 * transactions. If an operation fails, JPA automatically rolls back all changes, protecting database integrity.
 */
@Service
@Transactional
public class AiModelConfigService {

    private final AiModelConfigRepository repository;
    private final EncryptionService encryptionService;

    /**
     * Constructor Injection: Spring Boot automatically injects the repository and encryption beans.
     * Making fields final ensures thread-safety and immutability.
     */
    public AiModelConfigService(AiModelConfigRepository repository, EncryptionService encryptionService) {
        this.repository = repository;
        this.encryptionService = encryptionService;
    }

    /**
     * @PostConstruct is a lifecycle annotation. It tells Spring: "Execute this method
     * immediately after the bean is created, fully initialized, and all dependencies are injected."
     *
     * We use this to seed a default, free local Ollama configuration if the database is empty,
     * ensuring MyOS works instantly without requiring commercial cloud keys.
     */
    @PostConstruct
    public void seedDefaultConfiguration() {
        if (repository.countAll() == 0) {
            AiModelConfig defaultOllama = AiModelConfig.builder()
                    .provider("OLLAMA")
                    .modelName("llama3")
                    .baseUrl("http://localhost:11434")
                    .apiKey(null)
                    .isActive(true)
                    .build();
            repository.save(defaultOllama);
        }
    }

    /**
     * Fetches all registered AI configurations.
     * We mask the API keys before returning them to prevent credentials leakage in transit.
     */
    @Transactional(readOnly = true)
    public List<AiModelConfig> getAllConfigs() {
        List<AiModelConfig> configs = repository.findAll();
        configs.forEach(config -> {
            if (config.getApiKey() != null) {
                config.setApiKey("••••••••");
            }
        });
        return configs;
    }

    /**
     * Retrieves the single active AI configuration.
     * Throws an exception if none is marked active.
     */
    @Transactional(readOnly = true)
    public AiModelConfig getActiveConfig() {
        return repository.findByIsActiveTrue()
                .orElseThrow(() -> new MyOsException("No active AI model configuration found in database!", ErrorCode.MODEL_CONFIG_NOT_FOUND));
    }

    /**
     * Saves a model configuration.
     * If a new API key is provided, it is encrypted via EncryptionService before saving.
     * If the payload masks credentials with "••••••••", we load the existing encrypted key.
     */
    public AiModelConfig saveConfig(AiModelConfig config) {
        if (config.getApiKey() != null && !config.getApiKey().equals("••••••••") && !config.getApiKey().isBlank()) {
            config.setApiKey(encryptionService.encrypt(config.getApiKey()));
        } else if (config.getId() != null) {
            AiModelConfig existing = repository.findById(config.getId()).orElse(null);
            if (existing != null) {
                config.setApiKey(existing.getApiKey());
            }
        }
        
        return repository.save(config);
    }

    /**
     * Activates a model. Marks all other models as inactive to protect the system invariant
     * that only one model can be active at a time.
     *
     * @param id The UUID of the model to activate.
     */
    public void activateConfig(UUID id) {
        AiModelConfig target = repository.findById(id)
                .orElseThrow(() -> new MyOsException("Configuration not found with ID: " + id, ErrorCode.MODEL_CONFIG_NOT_FOUND));

        // Iterate and set all other active models to false
        repository.findAll().forEach(config -> {
            if (config.isActive() && !config.getId().equals(id)) {
                config.setActive(false);
                repository.save(config);
            }
        });

        // Set target active
        target.setActive(true);
        repository.save(target);
    }

    /**
     * Deletes a model configuration.
     * Blocks deletion of currently active configurations to prevent system crash.
     */
    public void deleteConfig(UUID id) {
        AiModelConfig target = repository.findById(id)
                .orElseThrow(() -> new MyOsException("Configuration not found with ID: " + id, ErrorCode.MODEL_CONFIG_NOT_FOUND));

        if (target.isActive()) {
            throw new MyOsException("Cannot delete an active configuration. Please activate another configuration first.", ErrorCode.ACTIVE_MODEL_REQUIRED);
        }

        repository.delete(target);
    }
    
    /**
     * Decrypts the target configuration's API key.
     * Used internally by the Dynamic Factory to build connection clients.
     */
    @Transactional(readOnly = true)
    public String getDecryptedApiKey(UUID id) {
        AiModelConfig config = repository.findById(id)
                .orElseThrow(() -> new MyOsException("Configuration not found with ID: " + id, ErrorCode.MODEL_CONFIG_NOT_FOUND));
        return encryptionService.decrypt(config.getApiKey());
    }
}
