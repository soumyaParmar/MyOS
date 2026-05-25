-- V13: Alter embedding column dimension to match Ollama's nomic-embed-text (768 dimensions)
-- Our V11 migration defined the dimension as 1536 (OpenAI standard).
-- However, we are running Ollama with 'nomic-embed-text' locally, which outputs 768 dimensions.
-- This migration updates the database column constraint to prevent dimension mismatch errors.

ALTER TABLE knowledge_base 
ALTER COLUMN embedding TYPE vector(768);
