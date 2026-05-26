package com.myos.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Configuration tells Spring Boot that this class defines bean factory methods.
 *
 * This configuration class resolves the "NoUniqueBeanDefinitionException" for EmbeddingModel.
 * Because we added the OpenAI starter, Spring AI attempts to auto-configure both the
 * Ollama EmbeddingModel and the OpenAI EmbeddingModel.
 *
 * The PostgreSQL PGVector vector store relies on an EmbeddingModel bean to index document snippets.
 * Since multiple embedding model beans are on the classpath, Spring encounters ambiguity.
 *
 * We explicitly mark the OllamaEmbeddingModel as the @Primary bean because:
 * 1. Our PGVector table is configured for 768-dimensions (matching nomic-embed-text of Ollama).
 * 2. OpenAI uses a 1536-dimension format by default, which would crash PostgreSQL.
 */
@Configuration
public class EmbeddingConfig {

    /**
     * @Bean tells Spring to manage the returned object as a managed bean.
     * @Primary designates this as the primary bean for dependency injection lookup.
     */
    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(OllamaEmbeddingModel ollamaEmbeddingModel) {
        return ollamaEmbeddingModel;
    }
}
