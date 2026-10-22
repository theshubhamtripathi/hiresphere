import api from './axios';

export const userApi = {
  me: () => api.get('/api/users/me').then((r) => r.data.data),
  updateProfile: (payload) => api.put('/api/users/me', payload).then((r) => r.data.data),
  changePassword: (payload) => api.put('/api/users/me/password', payload).then((r) => r.data),
  preferences: () => api.get('/api/preferences').then((r) => r.data.data),
  savePreferences: (payload) => api.put('/api/preferences', payload).then((r) => r.data.data),
};
