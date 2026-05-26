import api from '@/lib/api';

/**
 * Interface representing a database-backed AI Model Configuration.
 */
export interface AiModelConfig {
  id?: string;
  provider: 'OLLAMA' | 'OPENAI' | 'ANTHROPIC';
  modelName: string;
  baseUrl?: string;
  apiKey?: string;
  active: boolean; // Maps to isActive in the backend
}

export const aiModelService = {
  /**
   * Fetches all registered AI model configurations from the database.
   * Endpoint: GET /api/ai-models
   */
  async getAllModels(): Promise<AiModelConfig[]> {
    const response = await api.get<AiModelConfig[]>('/ai-models');
    return response.data;
  },

  /**
   * Saves or updates an AI model configuration in PostgreSQL.
   * Endpoint: POST /api/ai-models
   */
  async saveModel(config: Partial<AiModelConfig>): Promise<AiModelConfig> {
    const response = await api.post<AiModelConfig>('/ai-models', config);
    return response.data;
  },

  /**
   * Sets a specific AI model configuration as active, deactivating all others.
   * Endpoint: PUT /api/ai-models/{id}/activate
   */
  async activateModel(id: string): Promise<void> {
    await api.put(`/ai-models/${id}/activate`);
  },

  /**
   * Deletes a model configuration from the database.
   * Cannot delete currently active model configurations.
   * Endpoint: DELETE /api/ai-models/{id}
   */
  async deleteModel(id: string): Promise<void> {
    await api.delete(`/ai-models/${id}`);
  },
};
