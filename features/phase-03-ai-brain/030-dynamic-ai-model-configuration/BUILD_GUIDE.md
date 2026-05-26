# Build Guide: Dynamic AI Model Configuration (Full-Stack Tutorial)

Welcome to a truly premium, full-stack tutorial for **Phase 3 — AI Brain & Agent Graph**! 

In this comprehensive guide, you will build a dynamic, database-backed AI model configuration panel for MyOS. Instead of editing properties files, restarting servers, or committing API keys to Git, you will enable users to manage their LLMs directly from a Next.js UI dashboard.

We will store configurations in PostgreSQL, symmetric-encrypt sensitive keys (AES-256-GCM), construct a dynamic proxy `ChatModel` bean, and build a beautiful, glassmorphic settings dashboard to view, add, configure, and activate providers on the fly!

---

## 1. Significance: Why This Feature Matters

In static Spring Boot configurations, a `ChatModel` bean is instantiated at startup using static properties. If you change a setting, you have to rebuild and restart the entire server.

By shifting this configuration into a **PostgreSQL Database** and leveraging a **Custom Dynamic Proxy Pattern**:
1. **Zero Downtime Updates**: Users can add, modify, and swap models dynamically. The change propagates instantly across all agent nodes without server restarts.
2. **Encrypted Security at Rest**: API keys are symmetric-encrypted in the database, preventing leakage even if the database is backed up or compromised.
3. **Frictionless Developer Experience**: The backend automatically seeds a local Ollama configuration at startup, ensuring the system runs smoothly out of the box for anyone without any API keys!
4. **Professional UI Accessibility**: The frontend settings dashboard provides clear, glassmorphic cards and dynamic, conditional form elements that adapt to user selections (pre-filling local addresses for Ollama and showing key inputs for cloud platforms).

---

## 2. What To Do: Clear Task Definitions

To complete this task, you will follow a two-phased, full-stack workflow:

### Part A: Backend Implementation
1. **Create Flyway Database Migration**: Define the table for storing provider setups.
2. **Create Encryption Service**: Build a utility to encrypt/decrypt API keys securely.
3. **Build the JPA Persistence Layer**: Create the `AiModelConfig` entity, JpaRepository, and Transactional service layer.
4. **Implement REST Controller**: Expose secure CRUD and activation endpoints.
5. **Implement `DynamicChatModelFactory`**: Create a custom `@Primary ChatModel` bean that dynamically delegates all API calls to the active model configuration.
6. **Implement Seeding**: Seed a default active local Ollama model if the database has no records.

### Part B: Frontend Implementation
1. **Create the API Service Layer**: Build a service to communicate with the backend `/api/ai-models` endpoints.
2. **Build the Dashboard Component**: Create the settings page layout with high-end glassmorphism and responsiveness.
3. **Build the Dynamic Form Modal**: Create an interactive form that conditionally shows/hides fields depending on the selected provider.
4. **Add State Management & Alerts**: Coordinate loading, saving, and deletion states with user-friendly toast alerts.

---

## 3. How To Do It: Step-by-Step Instructions

Let's build this layered architecture!

---

### Part A: Backend Implementation

#### Step 3.A.1: Create Flyway Migration

Create a new file: `backend/src/main/resources/db/migration/V14__create_ai_model_config.sql`

```sql
CREATE TABLE ai_model_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(50) NOT NULL, -- OLLAMA, OPENAI, ANTHROPIC
    model_name VARCHAR(100) NOT NULL, -- e.g. llama3, gpt-4o, claude-3-5-sonnet
    base_url VARCHAR(255), -- e.g. http://localhost:11434 for Ollama
    api_key VARCHAR(512), -- Encrypted AES text
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index to enforce that only one model can be active at any given time
CREATE UNIQUE INDEX idx_only_one_active_model ON ai_model_configs (is_active) WHERE is_active = TRUE;
```

---

#### Step 3.A.2: Build the Encryption Service

Create a new utility class for symmetric encryption: `backend/src/main/java/com/myos/service/EncryptionService.java`

```java
package com.myos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * EncryptionService provides secure AES-GCM 256-bit symmetric encryption
 * for encrypting and decrypting cloud API keys at rest in PostgreSQL.
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bit length of auth tag
    private static final int IV_LENGTH = 12; // recommended GCM IV length

    private final SecretKeySpec keySpec;

    public EncryptionService(@Value("${app.security.encryption-key:FNE0Pp6yrzuWKEWgkPWx6pj4A8ibUeaCy/3KPFsHfz0=}") String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        this.keySpec = new SecretKeySpec(decodedKey, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv); // Cryptographically secure random IV

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while encrypting sensitive key", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            int cipherTextLen = combined.length - IV_LENGTH;
            byte[] encryptedBytes = new byte[cipherTextLen];
            System.arraycopy(combined, iv.length, encryptedBytes, 0, cipherTextLen);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while decrypting sensitive key", e);
        }
    }
}
```

---

#### Step 3.A.3: Create the JPA Entity and Repository

Create the JPA Entity: `backend/src/main/java/com/myos/entity/AiModelConfig.java`

```java
package com.myos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_model_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String provider; // OLLAMA, OPENAI, ANTHROPIC

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "api_key", length = 512)
    private String apiKey;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
```

Create the Repository interface: `backend/src/main/java/com/myos/repository/AiModelConfigRepository.java`

```java
package com.myos.repository;

import com.myos.entity.AiModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiModelConfigRepository extends JpaRepository<AiModelConfig, UUID> {

    Optional<AiModelConfig> findByIsActiveTrue();

    @Query("SELECT COUNT(a) FROM AiModelConfig a")
    long countAll();
}
```

---

#### Step 3.A.4: Build the Service Layer

Create: `backend/src/main/java/com/myos/service/AiModelConfigService.java`

```java
package com.myos.service;

import com.myos.entity.AiModelConfig;
import com.myos.repository.AiModelConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AiModelConfigService {

    private final AiModelConfigRepository repository;
    private final EncryptionService encryptionService;

    public AiModelConfigService(AiModelConfigRepository repository, EncryptionService encryptionService) {
        this.repository = repository;
        this.encryptionService = encryptionService;
    }

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

    @Transactional(readOnly = true)
    public AiModelConfig getActiveConfig() {
        return repository.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active AI model configuration found in database!"));
    }

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

    public void activateConfig(UUID id) {
        AiModelConfig target = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with ID: " + id));

        repository.findAll().forEach(config -> {
            if (config.isActive() && !config.getId().equals(id)) {
                config.setActive(false);
                repository.save(config);
            }
        });

        target.setActive(true);
        repository.save(target);
    }

    public void deleteConfig(UUID id) {
        AiModelConfig target = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with ID: " + id));

        if (target.isActive()) {
            throw new IllegalStateException("Cannot delete an active configuration. Please activate another configuration first.");
        }

        repository.delete(target);
    }
    
    @Transactional(readOnly = true)
    public String getDecryptedApiKey(UUID id) {
        AiModelConfig config = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with ID: " + id));
        return encryptionService.decrypt(config.getApiKey());
    }
}
```

---

#### Step 3.A.5: Build the Dynamic ChatModel Factory

Create: `backend/src/main/java/com/myos/config/DynamicChatModelFactory.java`

This class implements the **Proxy Pattern**. It delegates prompt calls dynamically to an underlying `ChatModel` client initialized at runtime based on database configurations:

```java
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

@Component
@Primary
public class DynamicChatModelFactory implements ChatModel {

    private final AiModelConfigService configService;

    private ChatModel cachedClient;
    private UUID cachedConfigId;
    private String cachedModelName;

    public DynamicChatModelFactory(AiModelConfigService configService) {
        this.configService = configService;
    }

    private synchronized ChatModel getActiveClient() {
        AiModelConfig activeConfig = configService.getActiveConfig();

        if (cachedClient == null || !activeConfig.getId().equals(cachedConfigId) || !activeConfig.getModelName().equals(cachedModelName)) {
            this.cachedClient = buildClient(activeConfig);
            this.cachedConfigId = activeConfig.getId();
            this.cachedModelName = activeConfig.getModelName();
        }

        return cachedClient;
    }

    private ChatModel buildClient(AiModelConfig config) {
        String provider = config.getProvider().toUpperCase();
        return switch (provider) {
            case "OLLAMA" -> {
                OllamaApi api = new OllamaApi(config.getBaseUrl());
                yield new OllamaChatModel(api);
            }
            case "OPENAI" -> {
                String decryptedKey = configService.getDecryptedApiKey(config.getId());
                OpenAiApi api = new OpenAiApi(decryptedKey);
                yield new OpenAiChatModel(api);
            }
            case "ANTHROPIC" -> {
                String decryptedKey = configService.getDecryptedApiKey(config.getId());
                AnthropicApi api = new AnthropicApi(decryptedKey);
                yield new AnthropicChatModel(api);
            }
            default -> throw new IllegalArgumentException("Unsupported AI Provider: " + provider);
        };
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return getActiveClient().call(prompt);
    }
}
```

---

#### Step 3.A.6: Build the REST Controller

Create: `backend/src/main/java/com/myos/controller/AiModelConfigController.java`

```java
package com.myos.controller;

import com.myos.entity.AiModelConfig;
import com.myos.service.AiModelConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai-models")
public class AiModelConfigController {

    private final AiModelConfigService configService;

    public AiModelConfigController(AiModelConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<List<AiModelConfig>> getAllModels() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @PostMapping
    public ResponseEntity<AiModelConfig> saveModel(@RequestBody AiModelConfig config) {
        return ResponseEntity.ok(configService.saveConfig(config));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateModel(@PathVariable UUID id) {
        configService.activateConfig(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable UUID id) {
        configService.deleteConfig(id);
        return ResponseEntity.ok().build();
    }
}
```

---

### Part B: Frontend Implementation

#### Step 3.B.1: Create the API Client Service

Create a new file: `frontend-next/src/services/aiModelService.ts`

```typescript
export interface AiModelConfig {
  id?: string;
  provider: 'OLLAMA' | 'OPENAI' | 'ANTHROPIC';
  modelName: string;
  baseUrl?: string;
  apiKey?: string;
  active: boolean;
}

const API_BASE_URL = '/api/ai-models';

export const aiModelService = {
  async getAllModels(): Promise<AiModelConfig[]> {
    const response = await fetch(API_BASE_URL);
    if (!response.ok) throw new Error('Failed to load AI model configurations');
    return response.json();
  },

  async saveModel(config: Partial<AiModelConfig>): Promise<AiModelConfig> {
    const response = await fetch(API_BASE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(config),
    });
    if (!response.ok) throw new Error('Failed to save model configuration');
    return response.json();
  },

  async activateModel(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/${id}/activate`, {
      method: 'PUT',
    });
    if (!response.ok) throw new Error('Failed to activate configuration');
  },

  async deleteModel(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Failed to delete configuration');
    }
  }
};
```

---

#### Step 3.B.2: Create the Settings Dashboard Page

Create the settings view at `frontend-next/src/app/dashboard/settings/ai-models/page.tsx`:

```tsx
'use client';

import React, { useEffect, useState } from 'react';
import { aiModelService, AiModelConfig } from '@/services/aiModelService';
import { Cpu, Plus, Sparkles, Trash2, CheckCircle2, ShieldAlert } from 'lucide-react';

export default function AiModelsPage() {
  const [models, setModels] = useState<AiModelConfig[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [modalOpen, setModalOpen] = useState<boolean>(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Form State
  const [provider, setProvider] = useState<'OLLAMA' | 'OPENAI' | 'ANTHROPIC'>('OLLAMA');
  const [modelName, setModelName] = useState<string>('llama3');
  const [baseUrl, setBaseUrl] = useState<string>('http://localhost:11434');
  const [apiKey, setApiKey] = useState<string>('');

  useEffect(() => {
    loadModels();
  }, []);

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const loadModels = async () => {
    try {
      setLoading(true);
      const data = await aiModelService.getAllModels();
      setModels(data);
    } catch (err: any) {
      showToast(err.message || 'Failed to fetch models', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleProviderChange = (selected: 'OLLAMA' | 'OPENAI' | 'ANTHROPIC') => {
    setProvider(selected);
    if (selected === 'OLLAMA') {
      setModelName('llama3');
      setBaseUrl('http://localhost:11434');
      setApiKey('');
    } else if (selected === 'OPENAI') {
      setModelName('gpt-4o');
      setBaseUrl('');
      setApiKey('');
    } else if (selected === 'ANTHROPIC') {
      setModelName('claude-3-5-sonnet-20241022');
      setBaseUrl('');
      setApiKey('');
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await aiModelService.saveModel({
        provider,
        modelName,
        baseUrl: provider === 'OLLAMA' ? baseUrl : undefined,
        apiKey: provider !== 'OLLAMA' ? apiKey : undefined,
        active: false,
      });
      showToast(`${provider} configuration added successfully!`, 'success');
      setModalOpen(false);
      loadModels();
    } catch (err: any) {
      showToast(err.message || 'Save failed', 'error');
    }
  };

  const handleActivate = async (id: string) => {
    try {
      await aiModelService.activateModel(id);
      showToast('AI Model switched successfully!', 'success');
      loadModels();
    } catch (err: any) {
      showToast(err.message || 'Activation failed', 'error');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this configuration?')) return;
    try {
      await aiModelService.deleteModel(id);
      showToast('Configuration deleted successfully', 'success');
      loadModels();
    } catch (err: any) {
      showToast(err.message || 'Delete failed', 'error');
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-white p-8">
      {toast && (
        <div className={`fixed top-5 right-5 z-50 px-4 py-3 rounded-lg shadow-2xl flex items-center gap-3 animate-bounce border ${
          toast.type === 'success' ? 'bg-emerald-950 border-emerald-500 text-emerald-300' : 'bg-rose-950 border-rose-500 text-rose-300'
        }`}>
          {toast.type === 'success' ? <CheckCircle2 className="w-5 h-5" /> : <ShieldAlert className="w-5 h-5" />}
          <span>{toast.message}</span>
        </div>
      )}

      <div className="flex justify-between items-center mb-10">
        <div>
          <h1 className="text-4xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-violet-400 to-indigo-300 flex items-center gap-3">
            <Cpu className="w-10 h-10 text-indigo-400" />
            AI Brain Models
          </h1>
          <p className="text-slate-400 mt-2">Manage your Large Language Models. Switch dynamically between local and cloud providers.</p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold px-4 py-2.5 rounded-lg shadow-lg hover:shadow-indigo-500/25 transition-all duration-300"
        >
          <Plus className="w-5 h-5" />
          Add Model
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-500"></div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {models.map((model) => (
            <div
              key={model.id}
              className={`relative rounded-xl border bg-slate-900/60 backdrop-blur-md p-6 flex flex-col justify-between transition-all duration-300 hover:-translate-y-1 hover:shadow-2xl ${
                model.active
                  ? 'border-indigo-500 shadow-indigo-500/10 ring-1 ring-indigo-500'
                  : 'border-slate-800 hover:border-slate-700'
              }`}
            >
              <div>
                <div className="flex justify-between items-start mb-4">
                  <span className={`px-2.5 py-1 rounded-full text-xs font-bold uppercase tracking-wider ${
                    model.provider === 'OLLAMA' ? 'bg-slate-800 text-slate-300' :
                    model.provider === 'OPENAI' ? 'bg-teal-950 text-teal-300 border border-teal-800' :
                    'bg-amber-950 text-amber-300 border border-amber-800'
                  }`}>
                    {model.provider}
                  </span>
                  
                  {model.active && (
                    <span className="flex items-center gap-1 text-xs text-indigo-400 font-bold bg-indigo-950/50 border border-indigo-800/80 px-2 py-0.5 rounded-full">
                      <Sparkles className="w-3.5 h-3.5" />
                      Active System Model
                    </span>
                  )}
                </div>

                <h3 className="text-xl font-bold mb-1">{model.modelName}</h3>
                <p className="text-xs text-slate-500 font-mono mb-4 break-all">ID: {model.id}</p>

                <div className="space-y-2 text-sm mb-6">
                  {model.baseUrl && (
                    <div className="flex justify-between">
                      <span className="text-slate-500">Endpoint:</span>
                      <span className="font-mono text-slate-300">{model.baseUrl}</span>
                    </div>
                  )}
                  <div className="flex justify-between">
                    <span className="text-slate-500">Credentials:</span>
                    <span className="font-mono text-slate-300">
                      {model.provider === 'OLLAMA' ? 'Local System (None)' : 'Encrypted (••••••••)'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-between gap-4 mt-auto pt-4 border-t border-slate-800/60">
                {!model.active ? (
                  <button
                    onClick={() => model.id && handleActivate(model.id)}
                    className="flex-1 bg-slate-800 hover:bg-indigo-600 text-slate-300 hover:text-white font-medium py-2 rounded-lg text-sm transition-all duration-300 border border-slate-700 hover:border-indigo-500"
                  >
                    Activate Model
                  </button>
                ) : (
                  <span className="flex-1 text-center bg-indigo-950/30 text-indigo-400 font-semibold py-2 rounded-lg text-sm border border-indigo-900/40">
                    Currently Selected
                  </span>
                )}

                {!model.active && (
                  <button
                    onClick={() => model.id && handleDelete(model.id)}
                    className="p-2 bg-slate-800/40 hover:bg-rose-950 text-slate-400 hover:text-rose-400 rounded-lg border border-slate-800 hover:border-rose-900/60 transition-all duration-300"
                    title="Delete model"
                  >
                    <Trash2 className="w-5 h-5" />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Model Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 relative animate-in fade-in zoom-in duration-300">
            <h2 className="text-2xl font-extrabold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-violet-400 to-indigo-300">
              Configure New LLM
            </h2>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-1.5">Provider</label>
                <select
                  value={provider}
                  onChange={(e) => handleProviderChange(e.target.value as any)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-white focus:outline-none focus:border-indigo-500 transition-colors"
                >
                  <option value="OLLAMA">Ollama (Local LLM)</option>
                  <option value="OPENAI">OpenAI (Cloud GPT)</option>
                  <option value="ANTHROPIC">Anthropic (Claude)</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-400 mb-1.5">Model Name</label>
                {provider === 'OLLAMA' ? (
                  <input
                    type="text"
                    required
                    value={modelName}
                    onChange={(e) => setModelName(e.target.value)}
                    placeholder="e.g. llama3, mistral, qwen2.5"
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-white focus:outline-none focus:border-indigo-500 transition-colors"
                  />
                ) : (
                  <select
                    value={modelName}
                    onChange={(e) => setModelName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-white focus:outline-none focus:border-indigo-500 transition-colors"
                  >
                    {provider === 'OPENAI' ? (
                      <>
                        <option value="gpt-4o">gpt-4o</option>
                        <option value="gpt-4-turbo">gpt-4-turbo</option>
                        <option value="gpt-3.5-turbo">gpt-3.5-turbo</option>
                      </>
                    ) : (
                      <>
                        <option value="claude-3-5-sonnet-20241022">claude-3-5-sonnet-20241022</option>
                        <option value="claude-3-opus-20240229">claude-3-opus</option>
                        <option value="claude-3-haiku-20240307">claude-3-haiku</option>
                      </>
                    )}
                  </select>
                )}
              </div>

              {provider === 'OLLAMA' && (
                <div>
                  <label className="block text-sm font-medium text-slate-400 mb-1.5">Ollama Server Base URL</label>
                  <input
                    type="text"
                    required
                    value={baseUrl}
                    onChange={(e) => setBaseUrl(e.target.value)}
                    placeholder="http://localhost:11434"
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-white focus:outline-none focus:border-indigo-500 transition-colors font-mono"
                  />
                </div>
              )}

              {provider !== 'OLLAMA' && (
                <div>
                  <label className="block text-sm font-medium text-slate-400 mb-1.5">API Key</label>
                  <input
                    type="password"
                    required
                    value={apiKey}
                    onChange={(e) => setApiKey(e.target.value)}
                    placeholder="sk-proj-..."
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-white focus:outline-none focus:border-indigo-500 transition-colors font-mono"
                  />
                </div>
              )}

              <div className="flex gap-3 justify-end pt-4 border-t border-slate-800/60 mt-6">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold px-4 py-2 rounded-lg transition-colors text-sm"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold px-4 py-2 rounded-lg transition-all text-sm shadow-md hover:shadow-indigo-500/25"
                >
                  Save Model
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
```

---

## 4. Educational Concepts: Deep Dive

### Concept 1: The Proxy Design Pattern
We implement a **Proxy Pattern** on the backend using `DynamicChatModelFactory`. Instead of autowiring specific instances, we expose a single `@Primary ChatModel` delegator. The business services call standard prompt logic, while the proxy dynamic interceptor reads PostgreSQL, builds the target instance, handles the decryption layers, and routes the network request smoothly. This creates complete decoupling and prevents API updates from crashing the application.

### Concept 2: Enforcing Invariants with PostgreSQL Indexing
To prevent split-brain states (multiple active LLM configurations running simultaneously), we use a **partial unique constraint** in PostgreSQL:
```sql
CREATE UNIQUE INDEX idx_only_one_active_model ON ai_model_configs (is_active) WHERE is_active = TRUE;
```
This forces the database storage layer itself to block any transaction that violates the single-active model rule, guaranteeing high integrity under heavy thread concurrency.

### Concept 3: Conditional Rendering in React Forms
In modern web applications, presenting fields that aren't needed leads to high user cognitive load and input errors. By using simple logical gates (`{provider === 'OLLAMA' && <Component />}`), React dynamically mounts and unmounts DOM nodes, simplifying data validation and inputs.

---

## 5. Verification Plan

### Backend Unit Test
Verify transactional deactivations: `backend/src/test/java/com/myos/service/AiModelConfigServiceTest.java`

```java
package com.myos.service;

import com.myos.entity.AiModelConfig;
import com.myos.repository.AiModelConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiModelConfigServiceTest {

    @Autowired
    private AiModelConfigService service;

    @Autowired
    private AiModelConfigRepository repository;

    @Test
    void testOnlyOneActiveConfigRule() {
        repository.deleteAll();

        AiModelConfig config1 = AiModelConfig.builder()
                .provider("OLLAMA").modelName("llama3").isActive(true).build();
        AiModelConfig config2 = AiModelConfig.builder()
                .provider("OPENAI").modelName("gpt-4o").isActive(false).build();

        config1 = repository.save(config1);
        config2 = repository.save(config2);

        service.activateConfig(config2.getId());

        AiModelConfig updatedConfig1 = repository.findById(config1.getId()).orElseThrow();
        AiModelConfig updatedConfig2 = repository.findById(config2.getId()).orElseThrow();

        assertThat(updatedConfig1.isActive()).isFalse();
        assertThat(updatedConfig2.isActive()).isTrue();
    }
}
```

### Manual Verification
1. Recompile backend: `./mvnw clean test-compile`
2. Run test suite: `./mvnw test -Dtest=AiModelConfigServiceTest`
3. Launch backend: `./mvnw spring-boot:run`
4. Access settings dashboard on Next.js frontend: `/dashboard/settings/ai-models`
5. Verify conditional form pre-fills, secure key submissions, dynamic card switching, and deletes.
