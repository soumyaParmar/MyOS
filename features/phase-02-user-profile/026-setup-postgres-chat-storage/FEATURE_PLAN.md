# Feature Plan: Set up PostgreSQL for AI Conversations and Agent Logs

This plan covers the implementation of PostgreSQL tables to store AI conversation history and agent logs, replacing the previous MongoDB strategy. This keeps our data infrastructure consolidated within PostgreSQL.

## Source Checkbox
- [ ] Set up PostgreSQL tables for AI conversations and agent logs (from `MyOS_TODO.md` - Phase 2)

## Goal and User Value
Enable stateful, long-term memory for AI agents using our existing PostgreSQL infrastructure. This allows users to review their chat history and provides agents with the context needed for consistent interactions.

## Scope
- Design the relational schema for conversations and messages.
- Create Flyway migration scripts for the new tables.
- Create `Conversation` and `ChatMessage` entities.
- Implement repositories for persisting and retrieving chat history.
- Ensure efficient querying (e.g., fetching the last N messages for an agent).

## Out of Scope
- PGVector setup (this will be handled in the next task, though it will use the same database instance).
- Implementation of the full AI Brain (Phase 3).

## Backend Changes

### Schema Design
- `conversations` table:
    - `UUID id` (PK)
    - `UUID user_id` (FK to users)
    - `String agent_type` (JOB, FINANCE, etc.)
    - `Timestamp created_at`
    - `Timestamp updated_at`
- `chat_messages` table:
    - `UUID id` (PK)
    - `UUID conversation_id` (FK to conversations)
    - `String role` (USER, ASSISTANT, SYSTEM)
    - `Text content`
    - `Timestamp created_at`
    - `Timestamp updated_at`

### Flyway Migrations
- Create `V7__create_chat_tables.sql` (or next sequence number).

### Entities
- `Conversation` (JPA Entity)
- `ChatMessage` (JPA Entity)

### Repositories
- `ConversationRepository`
- `ChatMessageRepository`

## Infrastructure Changes
No changes needed to `docker-compose.yml` for this specific task, as we are using the existing PostgreSQL service. (Note: The image will be updated to `pgvector/pgvector` in the next task).

## Security and Privacy
- Messages must be strictly isolated by `user_id`.
- Use soft deletes if needed for data retention policies.

## Testing and Verification Plan
- **Automated Tests**:
    - Repository tests to verify saving and retrieving conversation threads.
    - Test that messages are correctly ordered by `created_at`.
- **Manual Verification**:
    - Verify table creation via database client.
    - Manually insert and retrieve a sample conversation via a test service or CLI.

## Acceptance Criteria
- [ ] Chat tables are created in PostgreSQL via Flyway.
- [ ] `Conversation` and `ChatMessage` entities are correctly mapped.
- [ ] Repository can successfully retrieve history for a specific user and agent.
- [ ] All Java files include educational comments.

## Open Questions
- Should we use a JSONB column for message metadata (e.g., token usage, tool calls)? (Proposed: Yes, for future flexibility).
