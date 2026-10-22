import axios from 'axios';
import { store } from '../app/store';
import { tokensRefreshed, loggedOut } from '../features/auth/authSlice';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';

const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = store.getState().auth.accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshing = null;

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;
    const refreshToken = store.getState().auth.refreshToken;

    if (status === 401 && !original._retry && refreshToken && !original.url?.includes('/api/auth/')) {
      original._retry = true;
      try {
        refreshing = refreshing || axios.post(`${baseURL}/api/auth/refresh`, { refreshToken });
        const { data } = await refreshing;
        refreshing = null;
        const payload = data.data;
        store.dispatch(tokensRefreshed(payload));
        original.headers.Authorization = `Bearer ${payload.accessToken}`;
        return api(original);
      } catch (refreshError) {
        refreshing = null;
        store.dispatch(loggedOut());
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

/** Extracts a friendly message from an Axios error. */
export function errorMessage(error, fallback = 'Something went wrong') {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.fieldErrors?.[0]?.message ||
    error?.message ||
    fallback
  );
}

export default api;
