# Build Guide: Frontend Knowledge Base Management UI

Welcome, Learner! In the previous task, you successfully built the backend for the **Knowledge Ingestion Service**. 

In this build guide, we are going to build the **frontend UI** for managing your AI's knowledge base. We will also add search and delete capabilities on the backend to make the frontend fully complete. 

This UI will allow you to manually feed your AI new information, search through it semantically using **PGVector**, and delete snippets when they are no longer needed.

---

## Significance

Without a user interface, your personal AI's memory is a black box. This feature provides a **dashboard window** into your AI's brain. 

By building this page, you create the primary interface for **RAG (Retrieval-Augmented Generation)** ingestion. You will also see first-hand how mathematical vector numbers represent the *semantic meaning* of human sentences, showing similarity scores for matching topics even if they don't share the exact same words!

---

## Educational Concepts Deep Dive

### 1. Database Triggers (PostgreSQL PL/pgSQL)
A **trigger** is a specialized function that runs automatically in response to certain events (like `INSERT`, `UPDATE`, or `DELETE`) on a particular database table.
- **Why we need it here**: Spring AI's standard `VectorStore` starter is hardcoded to insert data into 4 specific columns (`id`, `content`, `metadata`, `embedding`). However, our database table demands a `user_id` column to prevent cross-user data leaks.
- **The Solution**: We put the `user_id` inside the `metadata` JSONB block. Before PostgreSQL saves the row, our trigger intercepts the insert operation, reads the `user_id` out of the JSONB block, and writes it directly to the physical `user_id` column. This combines the flexibility of Spring AI with the strict security of standard relational schemas!

### 2. Semantic Search vs. Traditional Search
- **Traditional (Lexical) Search**: Looks for exact word matches (e.g., searching for "dog" won't find articles with "puppy" unless the word "dog" is also present).
- **Semantic (Vector) Search**: Converts sentences into high-dimensional coordinate points called **embeddings**. The distance between these points represents how close their *meanings* are. Searching for "puppy" will easily find "young canine" or "golden retriever" because their vector directions are extremely similar in mathematical space!

### 3. Next.js 19 Client-Side State & API Interceptors
- **`useActionState` & `useState`**: We use React state to capture form inputs and display animated states.
- **Axios Interceptor**: We leverage our existing `api.ts` axios client. When we make requests to `/api/knowledge`, the axios client automatically forwards the session cookie, handles authorization, and executes silent token refreshes if our access token has expired.

---

## Step-by-Step Implementation Plan

### Phase 1: Database Trigger Migration

Create a new Flyway migration file in `backend/src/main/resources/db/migration/V12__add_knowledge_user_id_trigger.sql`:

```sql
-- V12: Add database trigger to automatically map user_id from metadata JSONB
CREATE OR REPLACE FUNCTION set_knowledge_base_user_id()
RETURNS TRIGGER AS $$
BEGIN
    -- Extract the user_id from the metadata jsonb object and set it to the user_id column
    IF NEW.user_id IS NULL AND NEW.metadata->>'user_id' IS NOT NULL THEN
        NEW.user_id := (NEW.metadata->>'user_id')::uuid;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_set_knowledge_base_user_id
BEFORE INSERT ON knowledge_base
FOR EACH ROW
EXECUTE FUNCTION set_knowledge_base_user_id();
```

---

### Phase 2: Refactor Ingestion Service (Backend)

Open `com.myos.service.KnowledgeIngestionService.java` and modify the `inject` method to find the authenticated user's ID and append it to the document metadata:

```java
package com.myos.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.myos.dto.KnowledgeRequest;
import com.myos.security.UserPrincipal; // Adjust this import based on your actual custom principal class name!

@Service
public class KnowledgeIngestionService {

    private final VectorStore vectorStore;

    public KnowledgeIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    public void inject(KnowledgeRequest request) {
        // 1. Get the current authenticated user's details from Spring Security Context
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId;
        
        if (principal instanceof UserPrincipal) {
            userId = ((UserPrincipal) principal).getId();
        } else {
            throw new IllegalStateException("User not authenticated in security context");
        }

        // 2. Build the metadata map
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_id", userId.toString()); // Crucial for database trigger and row ownership
        
        if (request.getSource() != null) {
            metadata.put("source", request.getSource());
        }
        if (request.getMetadata() != null) {
            metadata.putAll(request.getMetadata());
        }

        // 3. Create the Spring AI Document
        Document document = new Document(request.getContent(), metadata);

        // 4. Save to PGVector
        vectorStore.add(List.of(document));
    }
}
```

---

### Phase 3: Add Retrieval and Deletion Endpoints (Backend)

To support searching and deleting snippets, update `KnowledgeController.java` to look like this:

```java
package com.myos.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.myos.dto.KnowledgeRequest;
import com.myos.service.KnowledgeIngestionService;
import com.myos.security.UserPrincipal;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService knowledgeIngestionService;
    private final VectorStore vectorStore;

    public KnowledgeController(KnowledgeIngestionService knowledgeIngestionService, VectorStore vectorStore) {
        this.knowledgeIngestionService = knowledgeIngestionService;
        this.vectorStore = vectorStore;
    }

    @PostMapping
    public ResponseEntity<?> injectKnowledge(@RequestBody KnowledgeRequest request) {
        knowledgeIngestionService.inject(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Document>> searchKnowledge(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "15") int limit) {
        
        // 1. Get current user ID
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getId().toString();

        // 2. Perform a vector similarity search filtered by the current user ID
        // The filter expression ensures that the user CANNOT retrieve anyone else's snippets!
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(limit)
                .withSimilarityThreshold(0.1) // Returns matches with basic similarity
                .withFilterExpression("user_id == '" + userId + "'");

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSnippet(@PathVariable String id) {
        // Delete document by its UUID string representation from PGVector
        vectorStore.delete(List.of(id));
        return ResponseEntity.ok().build();
    }
}
```

---

### Phase 4: Create Frontend API Service

Create `frontend-next/src/services/knowledge.service.ts`:

```typescript
import api from '@/lib/api';

export interface KnowledgeSnippet {
  id: string;
  content: string;
  metadata: {
    source?: string;
    category?: string;
    user_id: string;
    [key: string]: any;
  };
  similarityScore?: number;
}

export interface IngestRequest {
  content: string;
  source: string;
  metadata: Record<String, any>;
}

export const knowledgeService = {
  async ingest(data: IngestRequest): Promise<void> {
    await api.post('/knowledge', data);
  },

  async search(query: string = '', limit: number = 20): Promise<KnowledgeSnippet[]> {
    const response = await api.get<KnowledgeSnippet[]>('/knowledge', {
      params: { query, limit },
    });
    return response.data;
  },

  async deleteSnippet(id: string): Promise<void> {
    await api.delete(`/knowledge/${id}`);
  },
};
```

---

### Phase 5: Build Ingestion Form & Snippet Cards

We will build modular, stylish UI components using Tailwind classes and Shadcn components.

#### Create Ingestion Panel Component
Create `frontend-next/src/components/knowledge/IngestForm.tsx` (marked with `"use client"`). It should contain:
- Text area for typing/pasting content.
- Dropdown or Select inputs for **Source** (e.g. `note`, `email`, `document`) and **Category** (e.g. `personal`, `work`, `learning`).
- A button with elegant hover animations and loading indicators while the API request is active.

#### Create Snippet Card Component
Create `frontend-next/src/components/knowledge/SnippetCard.tsx`. It should:
- Display the text segment with a neat scrollable height for long summaries.
- Render styled source and category badges (using colorful HSL hues instead of plain primary colors).
- Display the **Similarity Match Score** (if present) as a colorful pill (e.g., green for >80% match, yellow for 50-80% match).
- Include a small delete button (trash icon) to delete that specific card, triggering a clean fade-out effect.

---

### Phase 6: Build Knowledge Dashboard Page

Create `frontend-next/src/app/dashboard/knowledge/page.tsx`:
- Embed the `<Sidebar />` layout.
- Include a search field linked to state that fetches semantic vector results as you type (de-bounced) or on hitting Enter.
- Display `<IngestForm />` either in a beautiful popup Dialog or side-by-side on large screens.
- Display a list of snippets, handles empty states with a gorgeous vector empty illustration, and loading states using skeletal wireframe indicators.

---

## Verification & Testing Tasks

1. Run the Spring Boot application and Next.js frontend app.
2. Ingest two completely different facts:
   - "I am planning a trip to Kyoto, Japan, in October." (Source: `note`, Category: `personal`)
   - "Flyway migration scripts are executed in alphanumeric sequence." (Source: `document`, Category: `work`)
3. Search for: **"Asia travel plan"**.
4. **Expected Result**: The UI should rank the Kyoto note as the #1 search match with a high similarity score, even though the words "Asia" or "travel plan" are not written anywhere in that note! 
5. Delete the Flyway snippet, search again, and verify it has disappeared from the system.
