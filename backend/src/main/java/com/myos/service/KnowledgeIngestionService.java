package com.myos.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.myos.dto.KnowledgeRequest;


@Service
public class KnowledgeIngestionService {

    private final VectorStore vectorStore;

    public KnowledgeIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    public void inject(KnowledgeRequest request){
        
        Map<String,Object> doc = new HashMap<>();

        if(request.getSource() != null){
            doc.put("source", request.getSource());
        }

        if(request.getMetadata() != null){
            doc.putAll(request.getMetadata());
        }

        Document document = new Document(request.getContent(),doc);

        vectorStore.add(List.of(document));
    }
}
