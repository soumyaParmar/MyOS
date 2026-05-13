-- Add email_hash column for indexed lookups after email is encrypted.
-- Encrypted email values are non-deterministic (random IV per encryption),
-- so we store a SHA-256 hash for WHERE-clause lookups.
ALTER TABLE users ADD COLUMN email_hash VARCHAR(64);

-- Drop the old plaintext email index (email column will hold ciphertext)
DROP INDEX IF EXISTS idx_users_email;

-- Remove the unique constraint on the encrypted email column
-- (ciphertext is always unique due to random IV, so the constraint is meaningless)
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;

-- Change preferences column from jsonb to text since it will store encrypted ciphertext
ALTER TABLE users ALTER COLUMN preferences TYPE text USING preferences::text;

-- The email_hash unique index and data population happen in V7 (Java migration)
