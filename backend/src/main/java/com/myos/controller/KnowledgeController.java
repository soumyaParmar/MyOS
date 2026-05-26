package com.myos.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myos.dto.KnowledgeRequest;
import com.myos.service.KnowledgeIngestionService;

/**
 * @RestController is a convenience annotation that combines @Controller and @ResponseBody.
 * It tells Spring Boot that this class is a Web Controller where every method automatically
 * serializes its return value directly into the HTTP response body (usually as JSON),
 * bypassing standard HTML template rendering.
 *
 * @RequestMapping("/api/knowledge") defines the base URL path prefix for all endpoints
 * defined inside this controller class.
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    // Dependency Injection: Injecting our business service layer.
    // Making it 'final' guarantees it is initialized exactly once during construction,
    // ensuring thread safety and immutability.
    private final KnowledgeIngestionService knowledgeIngestionService;

    /**
     * Constructor injection: Spring Boot scans and instantiates the KnowledgeIngestionService bean
     * and automatically injects it when creating the controller.
     */
    public KnowledgeController(KnowledgeIngestionService knowledgeIngestionService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
    }

    /**
     * @PostMapping maps HTTP POST requests to this method. Used for creating/adding data.
     * @RequestBody deserializes the incoming JSON request payload directly into a KnowledgeRequest DTO.
     * 
     * @return ResponseEntity representing the complete HTTP response (status code, headers, and body).
     */
    @PostMapping
    public ResponseEntity<?> injectKnowledge(@RequestBody KnowledgeRequest request) {
        knowledgeIngestionService.inject(request);
        // Returns a 200 OK HTTP status with no response body.
        return ResponseEntity.ok().build();
    }

    /**
     * @GetMapping maps HTTP GET requests to this method. Used for retrieving data.
     * @RequestParam binds query parameters (e.g. /api/knowledge?query=Kyoto&limit=5) to method arguments.
     * We supply default values to ensure the controller operates gracefully even if they are omitted.
     *
     * @return List of matching Spring AI Documents serialized as a JSON array.
     */
    @GetMapping
    public ResponseEntity<List<Document>> getKnowledge(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "15") int limit) {

        // Delegate query fetching to our service layer containing user isolation and PGVector logic.
        List<Document> docs = knowledgeIngestionService.getKnowledge(query, limit);

        return ResponseEntity.ok(docs);
    }

    /**
     * @DeleteMapping("/{id}") maps HTTP DELETE requests to this method.
     * @PathVariable extracts the dynamic path segment ({id}) from the URL and binds it to the argument.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKnowledge(@PathVariable String id) {
        knowledgeIngestionService.delete(id);
        return ResponseEntity.ok().build();
    }
}