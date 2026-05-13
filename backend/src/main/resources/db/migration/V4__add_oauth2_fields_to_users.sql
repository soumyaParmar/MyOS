-- Add OAuth2 fields to users table
ALTER TABLE users ADD COLUMN provider VARCHAR(50);
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);

-- Make password nullable to support OAuth2 users
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
