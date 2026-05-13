import api from '@/lib/api';

export const authService = {
  async register(data: any) {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  async login(data: any) {
    const response = await api.post('/auth/login', data);
    return response.data;
  },

  async refreshToken() {
    const response = await api.post('/auth/refresh-token');
    return response.data;
  },

  async logout() {
    await api.post('/auth/logout');
  },

  triggerSocialLogin(provider: 'google' | 'github') {
    // Now proxied through /oauth2 on the same domain
    window.location.href = `/oauth2/authorization/${provider}`;
  },
};
