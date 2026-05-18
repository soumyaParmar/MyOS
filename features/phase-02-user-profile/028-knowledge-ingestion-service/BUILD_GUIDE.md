# Build Guide: Personal Knowledge Ingestion Service

Welcome back! Now that we have PGVector set up and a Mock Embedding model (or real one) ready, it's time to build the service that actually puts data into your AI's memory.

This service is the "Writer" for your RAG (Retrieval-Augmented Generation) pipeline.

---

### Phase 1: Create the Data Transfer Object (DTO)

First, we need a way to receive data from the outside world.

**Task**: Create `KnowledgeRequest.java` in `com.myos.dto`.

- **Fields**: `content` (String), `source` (String), `metadata` (Map<String, Object>).
- **Why?**: DTOs separate our API contract from our internal logic.

---

### Phase 2: Create the Knowledge Ingestion Service

This is where the magic happens. We will use Spring AI's `VectorStore` interface.

**Task**: Create `KnowledgeIngestionService.java` in `com.myos.service`.

- Annotate with `@Service`.
- Inject `VectorStore`.
- Implement `ingest(KnowledgeRequest request)`.

**Educational Concept: The `Document` Object**
In Spring AI, data isn't just "text". It's a `Document`. A `Document` contains:
1. `content`: The raw text.
2. `metadata`: A Map of extra info (source, userId, etc.).
3. `id`: A unique identifier.

When you call `vectorStore.add(documents)`, Spring AI:
1. Takes the text.
2. Sends it to the `EmbeddingModel` to get a vector (a list of numbers).
3. Saves both the text, metadata, and the vector into PGVector.

---

### Phase 3: Expose the REST API

We need an endpoint so we can test this.

**Task**: Create `KnowledgeController.java` in `com.myos.controller`.

- Endpoint: `POST /api/knowledge/ingest`.
- It should call the service and return a success message.

---

### Phase 4: Verification

1. Start your application.
2. Use Postman or cURL to send a request:
   ```bash
   curl -X POST http://localhost:8080/api/knowledge/ingest \
        -H "Content-Type: application/json" \
        -d '{
          "content": "My favorite color is Deep Sea Blue.",
          "source": "manual",
          "metadata": {"category": "personal"}
        }'
   ```
3. Check your database:
   ```sql
   SELECT content, metadata FROM vector_store;
   ```

---

### Significance
Without this service, your AI is "forgetful". It only knows what was in its training data. By ingesting your personal notes and emails, you are giving the AI context that is unique to **you**. This is the foundation of a truly "Personal" AI Operating System.

---

### Educational Deep Dive: Dependency Injection
Notice how we don't do `new PgVectorStore(...)`. We just ask Spring for a `VectorStore`.
- **Spring AI Auto-configuration**: Because we added the `spring-ai-pgvector-store-spring-boot-starter`, Spring automatically creates a `VectorStore` bean for us.
- **Loose Coupling**: Our service doesn't care if it's PGVector, Pinecone, or Redis. It just talks to the `VectorStore` interface. This makes it easy to switch databases later!
