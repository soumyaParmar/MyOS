# Feature Plan: Dynamic AI Model Configuration (Full-Stack)

## Source Checkbox

- Phase: Phase 3 — AI Brain & Agent Graph
- Checkbox: `- [ ] Backend & Frontend: Dynamic database-backed AI Model Configuration & Dashboard UI`
- Current status: `[x]`

## Goal

Provide a robust, database-backed dynamic configuration system and frontend dashboard for MyOS's LLM components. Instead of hardcoding AI providers in property files or environment variables, MyOS will store model configurations in PostgreSQL. This allows users to dynamically add, edit, and toggle between local Ollama, OpenAI, and Anthropic Claude instances from a frontend dashboard with immediate runtime updates.

## Scope

### Backend
- **Flyway Database Migration**: Define table `ai_model_configs` to store provider configurations.
- **JPA Persistence Layer**: Create the `AiModelConfig` entity, repository, and service.
- **API Endpoints**: Build a secured controller for managing configurations:
  - `GET /api/ai-models`: List all stored models (with api-keys masked).
  - `POST /api/ai-models`: Create or update a model configuration.
  - `PUT /api/ai-models/{id}/activate`: Set a specific model as the active system model.
  - `DELETE /api/ai-models/{id}`: Delete a configuration.
- **API Key Security**: Implement an encryption service (`EncryptionService`) using AES-256 to securely store cloud API keys.
- **Dynamic Proxy Factory (`DynamicChatModelFactory`)**: Build a dynamic implementation of the Spring AI `ChatModel` interface that intercepts prompts and delegates calls to a dynamically initialized instance based on the active database record.
- **Local Ollama Seeding**: Insert a default local Ollama record (`http://localhost:11434` with model `llama3`) during database initialization/seeding if no active models are present.

### Frontend
- **Dashboard Configuration Page**: Create settings route `/dashboard/settings/ai-models`.
- **Card-based List View**: Display registered configurations in a modern grid using glassmorphic card designs, active badges, and activation toggles.
- **Dynamic Form Modal**: Build an "Add Model" modal with conditional inputs (Ollama hides API keys and pre-populates localhost; OpenAI/Claude displays masked API keys and model options).
- **Toast Notifications**: Wire success/error notifications for configurations, activations, and deletions.

## Out Of Scope

- Building the LangGraph4j stateful graph nodes or chat APIs (handled in subsequent checkboxes).

## Backend Plan

### 1. Database Schema (`V14__create_ai_model_config.sql`)
```sql
CREATE TABLE ai_model_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(50) NOT NULL, -- OLLAMA, OPENAI, ANTHROPIC
    model_name VARCHAR(100) NOT NULL,
    base_url VARCHAR(255),
    api_key VARCHAR(512),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_only_one_active_model ON ai_model_configs (is_active) WHERE is_active = TRUE;
```

### 2. Encryption (`EncryptionService`)
- Symmetric AES-256-GCM encryption for cloud credentials using key derived from `ENCRYPTION_KEY`.
- Mask API keys (`••••••••`) when returning configurations via JSON REST API.

### 3. Dynamic Factory (`DynamicChatModelFactory`)
- Implement `ChatModel` interface, annotated with `@Primary`.
- Intercept all prompt executions and delegate to a dynamically instantiated `OllamaChatModel`, `OpenAiChatModel`, or `AnthropicChatModel` depending on the active database record.
- Implement synchronization and lazy caching to prevent redundant bean recreation.

## Frontend Plan

### 1. Route Layout
Create `/dashboard/settings/ai-models` inside the Next.js app.

### 2. UI Elements
- **Glassmorphic Cards Grid**: Show provider, model name, active indicators, and deletion triggers.
- **Dynamic Modal Form**:
  - Drops down: Ollama, OpenAI, Anthropic.
  - Dynamically mounts: Base URL (pre-fills `http://localhost:11434` for Ollama) or API Key (masks input for cloud platforms).

### 3. API Communication Layer
Link UI interactions to backend endpoints:
- `GET /api/ai-models`
- `POST /api/ai-models`
- `PUT /api/ai-models/{id}/activate`
- `DELETE /api/ai-models/{id}`

## Data And Migrations

- Flyway Migration `V14__create_ai_model_config.sql`.

## Security And Privacy

- **Encryption at Rest**: AES-256-GCM symmetric encryption for all cloud API keys.
- **Masked Transfers**: JSON payloads mask active credentials (`••••••••`) in transit.
- **Local Seeding**: No internet required or costs incurred by default, as the system seeds and executes on a local Ollama configuration out-of-the-box.

## Testing And Verification

- **Transactional DB Tests**: Assert activating model B deactivates model A.
- **Dynamic Factory Delegation**: Verify prompts dynamically route to the new target LLM upon activation.
- **Conditional Form Tests**: Assert form fields render dynamically.

## Acceptance Criteria

- [x] V14 Database Migration runs successfully.
- [x] Backend CRUD APIs and activation toggles work flawlessly.
- [x] API keys are encrypted at rest and masked in transit.
- [x] Primary dynamic proxy ChatModel executes prompts seamlessly.
- [x] Settings page renders registered models in premium glassmorphic cards.
- [x] Form modal dynamically displays fields based on LLM provider.
- [x] Dynamic activations update the system immediately.
