import api from '@/lib/api';
import { UserPreferencesResponseDTO, UserPreferencesRequestDTO } from '@/types/preferences';

/**
 * preferencesService
 * 
 * Handles all API communication related to user preferences.
 * This service layer decouples the UI components from the raw Axios calls.
 */
export const preferencesService = {
  /**
   * Fetches the current user's preferences from the backend.
   * Calls GET /api/v1/preferences
   */
  async getPreferences(): Promise<UserPreferencesResponseDTO> {
    const response = await api.get('/v1/preferences');
    return response.data;
  },

  /**
   * Updates the current user's preferences in the backend.
   * Calls PUT /api/v1/preferences
   * 
   * @param data The new preference settings to save.
   */
  async updatePreferences(data: UserPreferencesRequestDTO): Promise<UserPreferencesResponseDTO> {
    const response = await api.put('/v1/preferences', data);
    return response.data;
  },
};
