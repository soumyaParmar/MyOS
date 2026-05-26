-- Flyway Database Migration V14
-- Purpose: Create the database table to store dynamic, database-backed AI LLM configurations.
-- This table allows users to configure and toggle between local Ollama, OpenAI, and Anthropic.

CREATE TABLE ai_model_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(50) NOT NULL, -- The LLM engine type: OLLAMA, OPENAI, or ANTHROPIC
    model_name VARCHAR(100) NOT NULL, -- The specific model choice: e.g., llama3, gpt-4o, claude-3-5-sonnet
    base_url VARCHAR(255), -- The server API host URL (primarily utilized for local Ollama deployments)
    api_key VARCHAR(512), -- Symmetric AES-255-GCM encrypted string representing cloud credentials
    is_active BOOLEAN NOT NULL DEFAULT FALSE, -- Flag to identify which configuration is active system-wide
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Crucial Business Invariant: Only ONE model configuration can be active in the system at a time.
-- We enforce this at the database level by constructing a partial unique index.
-- This index prevents concurrent transactions from setting multiple rows to is_active = TRUE.
CREATE UNIQUE INDEX idx_only_one_active_model ON ai_model_configs (is_active) WHERE is_active = TRUE;
