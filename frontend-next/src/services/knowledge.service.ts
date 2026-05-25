import api from '@/lib/api';

/**
 * Interface representing a knowledge base snippet returned by the backend (PGVector).
 */
export interface KnowledgeSnippet {
  id: string;
  content: string;
  metadata: {
    source?: string;
    category?: string;
    user_id: string;
    [key: string]: any;
  };
  similarityScore?: number; // Embedded vector similarity match rating, if searched
}

/**
 * Interface representing the payload to ingest new knowledge.
 */
export interface IngestRequest {
  content: string;
  source: string;
  metadata?: Record<string, any>;
}

export const knowledgeService = {
  /**
   * Ingests raw text and metadata into the vector store.
   * Endpoint: POST /api/knowledge
   */
  async ingest(data: IngestRequest): Promise<void> {
    await api.post('/knowledge', data);
  },

  /**
   * Retrieves or semantically searches knowledge snippets.
   * Endpoint: GET /api/knowledge?query=...&limit=...
   */
  async search(query: string = '', limit: number = 20): Promise<KnowledgeSnippet[]> {
    const response = await api.get<KnowledgeSnippet[]>('/knowledge', {
      params: { query, limit },
    });
    return response.data;
  },

  /**
   * Deletes a knowledge snippet by ID from the vector database.
   * Endpoint: DELETE /api/knowledge/{id}
   */
  async deleteSnippet(id: string): Promise<void> {
    await api.delete(`/knowledge/${id}`);
  },
};
