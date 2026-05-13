-- Create the user_profiles table to store extended user information.
-- This table is linked 1:1 with the users table.
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    bio TEXT,
    skills TEXT,
    goals TEXT,
    resume_text TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    -- ON DELETE CASCADE ensures that if a user is deleted, their profile is also deleted.
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add comments for documentation at the database level
COMMENT ON TABLE user_profiles IS 'Stores detailed user information for AI personalization';
COMMENT ON COLUMN user_profiles.bio IS 'User biography (encrypted at rest)';
COMMENT ON COLUMN user_profiles.skills IS 'User skills (encrypted at rest)';
COMMENT ON COLUMN user_profiles.goals IS 'User goals (encrypted at rest)';
COMMENT ON COLUMN user_profiles.resume_text IS 'Full resume text (encrypted at rest)';
