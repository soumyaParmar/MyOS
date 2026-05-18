# Build Guide: PostgreSQL Chat Storage

Welcome to the implementation of the chat storage system! We are using PostgreSQL to store our AI conversation history, which keeps our infrastructure simple and consolidated.

## What we are building
A relational storage system for AI messages, consisting of:
1. **Conversations**: A grouping entity that ties messages to a specific user and agent type (e.g., Job Agent, Finance Agent).
2. **Messages**: The individual lines of dialogue between the user and the AI.

---

## Task 1: Create the Database Schema (Flyway)
We need two tables: `conversations` and `chat_messages`.

### Instructions
1. Create a new SQL migration file: `backend/src/main/resources/db/migration/V10__create_chat_tables.sql`.
2. Define the `conversations` table with `id`, `user_id`, `agent_type`, and timestamps.
3. Define the `chat_messages` table with a foreign key to `conversations`.

### Educational Concept: One-to-Many Relationships
In database design, a **Conversation** has many **Messages**. We use a **Foreign Key** (`conversation_id`) in the `chat_messages` table to point back to the parent conversation. This allows us to retrieve all messages for a specific chat session with a single query.

---

## Task 2: Implement the JPA Entities
Now, we'll map these tables to Java objects.

### Instructions
1. Create `Conversation.java` in `com.myos.entity`.
2. Create `ChatMessage.java` in `com.myos.entity`.
3. Use `@ManyToOne` in `ChatMessage` and `@OneToMany` in `Conversation`.

### Significance: JPA Annotations
*   `@Entity`: Tells Hibernate this class maps to a database table.
*   `@Enumerated(EnumType.STRING)`: Stores enum values as readable text (e.g., "USER", "ASSISTANT") instead of numbers.
*   `@ManyToOne`: Links many messages to one conversation.

---

## Task 3: Create the Repositories
We need to save and fetch these logs.

### Instructions
1. Create `ConversationRepository.java` in `com.myos.repository`.
2. Create `ChatMessageRepository.java` in `com.myos.repository`.
3. Add a method like `findByUserIdAndAgentType` to find existing conversations.

### Educational Concept: Spring Data JPA
Spring Data JPA automatically generates the SQL for you based on the method names you define (e.g., `findByConversationIdOrderByCreatedAtAsc`). This saves you from writing repetitive CRUD queries.

---

## Deep Dive: Why PostgreSQL for Chat?
While document databases like MongoDB are popular for chat, PostgreSQL is excellent because:
1. **Consistency**: Your chat history is always in sync with your user profile.
2. **ACID Transactions**: Ensuring a message is never lost or orphaned.
3. **JSONB Support**: If we need to add dynamic metadata later (like token usage or tool calls), PostgreSQL's JSONB columns provide "NoSQL-like" flexibility.

---

## Verification Checklist
- [ ] Run the application and check if Flyway successfully applies `V10`.
- [ ] Verify the tables exist in your PostgreSQL database (using `psql` or a GUI).
- [ ] Ensure that a `ChatMessage` cannot exist without a valid `Conversation` (Referential Integrity).

---

Ready to consolidate your AI memory? Let's build!
