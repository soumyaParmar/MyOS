-- Initial schema setup
CREATE TABLE IF NOT EXISTS app_config (
    id SERIAL PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Placeholder for initial system settings
INSERT INTO app_config (config_key, config_value) 
VALUES ('system.version', '0.0.1')
ON CONFLICT (config_key) DO NOTHING;
