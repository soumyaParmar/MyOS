package com.myos.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.myos.dto.KnowledgeRequest;
import com.myos.entity.User;

@Service
public class KnowledgeIngestionService {

    private final VectorStore vectorStore;

    public KnowledgeIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    private UUID getUserID() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getId();
        } else {
            throw new IllegalStateException("User not found");
        }
    }

    public void inject(KnowledgeRequest request) {

        UUID userID = getUserID();

        Map<String, Object> doc = new HashMap<>();

        doc.put("userId", userID.toString());

        if (request.getSource() != null) {
            doc.put("source", request.getSource());
        }

        if (request.getMetadata() != null) {
            doc.putAll(request.getMetadata());
        }

        Document document = new Document(request.getContent(), doc);

        vectorStore.add(List.of(document));
    }

    public List<Document> getKnowledge(String query, int limit) {

        String userID = getUserID().toString();

        SearchRequest srq = SearchRequest.query(query)
                .withTopK(limit).withSimilarityThreshold(0.1)
                .withFilterExpression("userId == '" + userID + "'");

        List<Document> result = vectorStore.similaritySearch(srq);

        return result;
    }

    public void delete(String id) {
        vectorStore.delete(List.of(id));
    }
}
