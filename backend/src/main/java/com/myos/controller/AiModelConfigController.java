package com.myos.controller;

import com.myos.entity.AiModelConfig;
import com.myos.service.AiModelConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @RestController maps this class as a RESTful web controller.
 * It combines @Controller and @ResponseBody, ensuring every returned object is serialized
 * directly into the HTTP response body as JSON.
 *
 * @RequestMapping("/api/ai-models") prefixes all endpoints with this base path.
 */
@RestController
@RequestMapping("/api/ai-models")
public class AiModelConfigController {

    private final AiModelConfigService configService;

    /**
     * Dependency Injection: constructor injection matches clean engineering patterns.
     */
    public AiModelConfigController(AiModelConfigService configService) {
        this.configService = configService;
    }

    /**
     * Retrieves the complete list of registered model configurations (masked).
     * Endpoint: GET /api/ai-models
     */
    @GetMapping
    public ResponseEntity<List<AiModelConfig>> getAllModels() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    /**
     * Saves or updates a model configuration (with automatic symmetric encryption).
     * Endpoint: POST /api/ai-models
     */
    @PostMapping
    public ResponseEntity<AiModelConfig> saveModel(@RequestBody AiModelConfig config) {
        return ResponseEntity.ok(configService.saveConfig(config));
    }

    /**
     * Sets a specific model as the system-wide active engine, deactivating all others.
     * Endpoint: PUT /api/ai-models/{id}/activate
     *
     * @param id UUID path parameter binding.
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateModel(@PathVariable UUID id) {
        configService.activateConfig(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a specific model configuration.
     * Endpoint: DELETE /api/ai-models/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable UUID id) {
        configService.deleteConfig(id);
        return ResponseEntity.ok().build();
    }
}
