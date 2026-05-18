import api from '@/lib/api';
import { UserProfileResponseDTO, UserProfileUpdateRequestDTO } from '@/types/profile';

export const profileService = {
  async getProfile(): Promise<UserProfileResponseDTO> {
    const response = await api.get('/v1/profile');
    return response.data;
  },

  async updateProfile(data: UserProfileUpdateRequestDTO): Promise<UserProfileResponseDTO> {
    const response = await api.put('/v1/profile', data);
    return response.data;
  },
};
