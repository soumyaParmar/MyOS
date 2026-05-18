-- V11: Enable PGVector extension and create Knowledge Base table
-- This migration upgrades our PostgreSQL database to support AI-powered semantic search.

-- 1. Enable the vector extension
-- This adds the 'vector' data type and similarity search operators (like <=> for cosine distance) to the database.
-- IMPORTANT: This migration will ONLY work if you have switched your Docker image to 'pgvector/pgvector' as instructed in Task 1.
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Create the knowledge_base table
-- This table stores 'embeddings' (mathematical representations of text meaning).
-- By storing these in Postgres, we can perform 'Semantic Search' alongside our relational data.
CREATE TABLE IF NOT EXISTS knowledge_base (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL, -- Links this knowledge snippet to a specific user (Security Requirement)
    content TEXT NOT NULL, -- The original text snippet (e.g., a paragraph from a PDF)
    metadata JSONB,        -- Stores dynamic info like 'source: document.pdf' or 'page: 5'
    embedding vector(1536), -- The vector representation (1536 is standard for OpenAI 'text-embedding-3-small')
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure each snippet belongs to a valid user and is removed if the user is deleted
    CONSTRAINT fk_knowledge_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Indexing for performance
-- We index user_id to ensure fast filtering when searching for a specific user's knowledge.
CREATE INDEX idx_knowledge_user_id ON knowledge_base(user_id);

-- Educational Note: Vector Indexes
-- For large datasets, you would add a specialized index like HNSW (Hierarchical Navigable Small World)
-- to make similarity searches extremely fast.
-- Example: CREATE INDEX ON knowledge_base USING hnsw (embedding vector_cosine_ops);
