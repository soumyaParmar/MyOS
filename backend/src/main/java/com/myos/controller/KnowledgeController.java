package com.myos.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myos.dto.KnowledgeRequest;
import com.myos.service.KnowledgeIngestionService;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService knowledgeIngestionService;

    public KnowledgeController(KnowledgeIngestionService knowledgeIngestionService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
    }

    @PostMapping
    public ResponseEntity<?> injectKnowledge(@RequestBody KnowledgeRequest request) {
        knowledgeIngestionService.inject(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Document>> getKnowledge(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "15") int limit) {

        List<Document> docs = knowledgeIngestionService.getKnowledge(query, limit);

        return ResponseEntity.ok(docs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKnowledge(@PathVariable String id) {
        knowledgeIngestionService.delete(id);
        return ResponseEntity.ok().build();
    }
}