-- Create the user_preferences table to store settings and preferences.
-- This table follows a 1:1 relationship with the users table.
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    job_types TEXT,
    monthly_budget_limit DOUBLE PRECISION,
    email_notifications_enabled BOOLEAN DEFAULT TRUE,
    push_notifications_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key to users table with ON DELETE CASCADE.
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Documentation comments
COMMENT ON TABLE user_preferences IS 'Stores user-specific settings and application preferences';
COMMENT ON COLUMN user_preferences.job_types IS 'Comma-separated list of preferred job types/roles';
COMMENT ON COLUMN user_preferences.monthly_budget_limit IS 'Monthly spending limit for the finance agent';
COMMENT ON COLUMN user_preferences.email_notifications_enabled IS 'Toggle for sending email notifications';
COMMENT ON COLUMN user_preferences.push_notifications_enabled IS 'Toggle for sending browser/mobile push notifications';
