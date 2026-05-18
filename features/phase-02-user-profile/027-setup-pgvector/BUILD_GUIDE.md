# Build Guide: Setting up PGVector

Welcome to the world of **Semantic Search**! In this task, we are upgrading our standard PostgreSQL database to support **Vectors**. This will allow our AI to search through your documents, emails, and notes based on their *meaning* rather than just keywords.

## What we are building
1. **Infrastructure Upgrade**: Switching our Docker image to one that supports the `vector` extension.
2. **Database Extension**: Enabling the extension in our SQL schema.
3. **Knowledge Base**: Creating a table to store your "embeddings" (mathematical representations of your text).

---

## Task 1: Update the Docker Image
Standard PostgreSQL doesn't know how to handle vectors. We need the `pgvector` specialized image.

### Instructions
1. Open `backend/docker-compose.yml`.
2. Change the `image` from `postgres:16-alpine` to `pgvector/pgvector:pg16`.
3. Restart your containers (run `docker-compose down` and `docker-compose up -d`).

### Educational Concept: Docker Images
Docker images are like "snapshots" of an operating system with specific software installed. By switching to `pgvector/pgvector`, we are essentially downloading a version of Postgres that comes pre-packaged with the tools needed for AI vector math.

---

## Task 2: Enable the Extension (Flyway)
Even though the software is installed, we need to tell the database to "turn on" the vector feature for our specific project.

### Instructions
1. Create a new migration file: `backend/src/main/resources/db/migration/V11__enable_pgvector_and_create_knowledge_table.sql`.
2. Add the command: `CREATE EXTENSION IF NOT EXISTS vector;`.

### Significance: PostgreSQL Extensions
PostgreSQL is "extensible," meaning you can add new features without changing the core database code. The `vector` extension adds a new data type called `vector` and new operators (like `<=>` for similarity search).

---

## Task 3: Create the Knowledge Table
We need a place to store "embeddings." An embedding is a long list of numbers (usually 1536 for OpenAI models) that represents the meaning of a piece of text.

### Instructions
In the same `V11` SQL file, create the `knowledge_base` table:
```sql
CREATE TABLE IF NOT EXISTS knowledge_base (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(1536) -- 1536 is the standard dimension for OpenAI/Claude
);
```

### Educational Concept: Vector Dimensions
Imagine a 3D space with X, Y, and Z coordinates. A vector is just a point in that space. In AI, we use a space with **1536 dimensions**! Words that have similar meanings will be placed close to each other in this massive invisible space.

---

## Task 4: Add Spring AI (Optional for now)
If you want to start using Java code to talk to this table, we need the Spring AI dependency.

### Instructions
Add this to your `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
</dependency>
```

---

## Verification Checklist
- [ ] Run `docker-compose ps` and ensure the database is running.
- [ ] Run the SQL command `SELECT * FROM pg_extension WHERE extname = 'vector';` in your DB console to verify it's active.
- [ ] Check if Flyway applied `V11` without errors.

Ready to give your AI a brain? Let's go!
