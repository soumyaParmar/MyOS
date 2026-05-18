# Feature Plan: Set up PGVector for Vector Embeddings and Search

This plan covers the transition from Pinecone to **PGVector**, allowing us to store and search vector embeddings directly within our PostgreSQL database. This further consolidates our infrastructure and simplifies data management.

## Source Checkbox
- [ ] Set up PGVector (PostgreSQL) for vector embeddings and search pipeline (from `MyOS_TODO.md` - Phase 2)

## Goal and User Value
Enable semantic search and Retrieval-Augmented Generation (RAG) using the same database that stores our relational data. This reduces complexity, latency, and cost while providing a unified storage solution for our AI knowledge base.

## Scope
- Update `docker-compose.yml` to use the `pgvector/pgvector` image.
- Create a migration to enable the `vector` extension in PostgreSQL.
- Add Spring AI dependencies (or relevant JDBC/JPA support for PGVector).
- Create a `KnowledgeBase` table with a `vector` column for embeddings.
- Implement a basic service to store and search embeddings.

## Out of Scope
- Implementation of the full Embedding Pipeline (handled in task 028).
- Frontend UI for knowledge base management (handled in task 029).

## Backend Changes

### Dependencies
- Add `org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter` (if using Spring AI).

### Infrastructure Changes
- Update `backend/docker-compose.yml`:
    ```yaml
    db:
      image: pgvector/pgvector:pg16
    ```

### Database Migrations
- `V8__enable_pgvector_and_create_embeddings_table.sql`:
    ```sql
    CREATE EXTENSION IF NOT EXISTS vector;
    CREATE TABLE IF NOT EXISTS knowledge_base (
        id UUID PRIMARY KEY,
        content TEXT,
        metadata JSONB,
        embedding vector(1536) -- Matches OpenAI/Claude embedding dimensions
    );
    ```

### Services
- `VectorStorageService`: Handles saving embeddings and performing similarity searches using the `<=>` (cosine distance) operator.

## Security and Privacy
- Embeddings must be linked to `user_id` to prevent cross-user data leakage in search results.

## Testing and Verification Plan
- **Automated Tests**:
    - Verify that the `vector` extension is successfully enabled.
    - Test similarity search: Insert 3 vectors and verify that searching for a similar vector returns the correct result.
- **Manual Verification**:
    - Run `SELECT * FROM pg_extension WHERE extname = 'vector';` to confirm installation.

## Acceptance Criteria
- [ ] PostgreSQL image is updated to support PGVector.
- [ ] `vector` extension is enabled in the database.
- [ ] `knowledge_base` table is created.
- [ ] Similarity search returns expected results in integration tests.

## Open Questions
- What embedding model will we use? (Default: OpenAI or a local HuggingFace model via Spring AI).
