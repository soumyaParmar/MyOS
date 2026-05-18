# Feature Plan: Personal Knowledge Ingestion Service

This plan outlines the implementation of the backend service responsible for ingesting personal knowledge (notes, emails, summaries) into the MyOS AI memory (PGVector).

- **Source Checkbox**: `MyOS_TODO.md` -> `- [ ] Create personal knowledge ingestion service (embed notes, emails, summaries)`

## Goal and User Value
To provide MyOS with "long-term memory" by storing user-provided information in a searchable vector format. This allows the AI agents to retrieve relevant context when answering queries (RAG - Retrieval Augmented Generation).

## Scope
- Backend service to process raw text into AI-searchable documents.
- Integration with `PgVectorStore` for storage.
- REST API endpoint for manual knowledge ingestion.
- Support for metadata (source, category, timestamp).

## Out of Scope
- Frontend UI for uploading documents (next task).
- Complex PDF/File parsing (focused on raw text for now).
- Background scheduled ingestion (manual trigger via API for now).

## Backend Changes

### 1. DTOs
- **`KnowledgeRequest`**: Request body for the ingestion API.
  - `String content`: The text to ingest.
  - `String source`: e.g., "manual", "email", "note".
  - `Map<String, Object> metadata`: Additional context.

### 2. Services
- **`KnowledgeIngestionService`**:
  - Uses `VectorStore` (injected bean).
  - Converts `KnowledgeRequest` to `org.springframework.ai.document.Document`.
  - Calls `vectorStore.add(List<Document>)`.

### 3. Controllers
- **`KnowledgeController`**:
  - `POST /api/knowledge/ingest`: Accepts `KnowledgeRequest` and triggers the ingestion.

## Data Model Changes
- No changes to relational tables. Data will be stored in the `vector_store` table managed by PGVector.

## API Contracts

### `POST /api/knowledge/ingest`
**Request Body**:
```json
{
  "content": "The project deadline is next Friday.",
  "source": "note",
  "metadata": {
    "priority": "high",
    "category": "work"
  }
}
```

**Response**:
- `200 OK` with a success message.

## Security and Privacy considerations
- Only authenticated users can ingest knowledge.
- (Future) Ensure knowledge is partitioned/filtered by `userId`.

## Testing and Verification Plan
- **Unit Test**: Mock `VectorStore` and verify `add()` is called with correct `Document` data.
- **Integration Test**: Verify the API endpoint responds correctly and data appears in PostgreSQL `vector_store` table.

## Acceptance Criteria
- [ ] A Spring Boot `@Service` exists for knowledge ingestion.
- [ ] A REST endpoint `/api/knowledge/ingest` is functional.
- [ ] Ingested text is correctly transformed into a `Document` with metadata.
- [ ] Data is persisted in the `vector_store` table in PostgreSQL.

## Open Questions
- Should we implement text splitting/chunking now, or let the service handle single chunks for now? (Answer: Keep it simple for now, ingest the whole text as one document if small).
