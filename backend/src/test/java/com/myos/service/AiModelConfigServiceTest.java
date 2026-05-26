package com.myos.service;

import com.myos.entity.AiModelConfig;
import com.myos.repository.AiModelConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @SpringBootTest spins up the full Spring Application Context (container),
 * allowing comprehensive verification of database interactions and service injections.
 *
 * @ActiveProfiles("test") ensures that our test-specific configurations are used.
 *
 * @Transactional ensures that each test method runs in its own transaction.
 * By default, Spring rolls back the transaction at the end of the test method,
 * keeping the database completely clean and isolated from side effects.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiModelConfigServiceTest {

    @Autowired
    private AiModelConfigService service;

    @Autowired
    private AiModelConfigRepository repository;

    @Test
    @DisplayName("Verify that activating a model configuration automatically deactivates all others")
    void testOnlyOneActiveConfigRule() {
        // Clean database state for deterministic testing
        repository.deleteAllInBatch();

        // Create two model configurations
        AiModelConfig config1 = AiModelConfig.builder()
                .provider("OLLAMA")
                .modelName("llama3")
                .isActive(true)
                .build();
        
        AiModelConfig config2 = AiModelConfig.builder()
                .provider("OPENAI")
                .modelName("gpt-4o")
                .isActive(false)
                .build();

        config1 = repository.save(config1);
        config2 = repository.save(config2);

        // Assert initial setup state
        assertThat(config1.isActive()).isTrue();
        assertThat(config2.isActive()).isFalse();

        // Execute activation operation on the second model
        service.activateConfig(config2.getId());

        // Reload data from repository
        AiModelConfig updatedConfig1 = repository.findById(config1.getId()).orElseThrow();
        AiModelConfig updatedConfig2 = repository.findById(config2.getId()).orElseThrow();

        // Assert business invariant has been successfully preserved:
        // Config1 must now be inactive, and Config2 must now be active.
        assertThat(updatedConfig1.isActive())
                .as("Config 1 should be automatically deactivated")
                .isFalse();
        
        assertThat(updatedConfig2.isActive())
                .as("Config 2 should be activated")
                .isTrue();
    }
}
