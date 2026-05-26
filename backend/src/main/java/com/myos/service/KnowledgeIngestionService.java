package com.myos.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.myos.dto.KnowledgeRequest;
import com.myos.entity.User;

/**
 * @Service tells Spring Boot that this class is a service bean containing key business logic.
 * Spring will manage its lifecycle and make it available for dependency injection.
 */
@Service
public class KnowledgeIngestionService {

    // Injecting the core Spring AI VectorStore component to store and search embeddings.
    private final VectorStore vectorStore;

    /**
     * Recommended constructor-based Dependency Injection.
     * Spring automatically resolves the VectorStore bean from our configuration.
     */
    public KnowledgeIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Helper method to fetch the active authenticated user's ID from Spring Security context.
     */
    private UUID getUserID() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getId();
        } else {
            throw new IllegalStateException("User not found in security context");
        }
    }

    /**
     * Ingests a new text document snippet, computes its embedding, and stores it in PGVector.
     */
    public void inject(KnowledgeRequest request) {
        UUID userID = getUserID();

        Map<String, Object> doc = new HashMap<>();
        
        // This metadata key is read by our database trigger to populate the 'user_id' column
        doc.put("user_id", userID.toString());

        if (request.getSource() != null) {
            doc.put("source", request.getSource());
        }

        if (request.getMetadata() != null) {
            doc.putAll(request.getMetadata());
        }

        Document document = new Document(request.getContent(), doc);
        vectorStore.add(List.of(document));
    }

    /**
     * Searches for semantically similar items, strictly isolated to the current user.
     */
    public List<Document> getKnowledge(String query, int limit) {
        String userID = getUserID().toString();

        // EDUCATIONAL NOTE: FilterExpressionBuilder
        // Spring AI parses raw string filter expressions (e.g. "user_id == '...'") using an ANTLR-based parser.
        // Due to ANTLR grammar constraints in older/specific versions of Spring AI, underscores ('_') 
        // in raw string keys often trigger "token recognition error at: '_'".
        // Using the programmatic FilterExpressionBuilder builds the filter object tree directly, 
        // completely bypassing string parsing and solving this lexer limitation beautifully.
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        SearchRequest srq = SearchRequest.query(query)
                .withTopK(limit)
                .withSimilarityThreshold(0.1)
                .withFilterExpression(b.eq("user_id", userID).build());

        List<Document> result = vectorStore.similaritySearch(srq);
        return result;
    }

    /**
     * Deletes a specific knowledge snippet from the vector store.
     */
    public void delete(String id) {
        vectorStore.delete(List.of(id));
    }
}

