import axios, { type InternalAxiosRequestConfig } from 'axios';
import type { LoginResponse, ReminderInput, ReminderPage, Reminder, NotificationPage, Notification } from './types';
import { tokenStore } from './tokenStore';

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/$/, '');

const api = axios.create({
  baseURL: apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
});

let refreshPromise: Promise<string | null> | null = null;

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStore.getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(undefined, async (error) => {
  const originalRequest = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
  const isAuthEndpoint = originalRequest?.url?.includes('/auth/');

  if (error.response?.status !== 401 || !originalRequest || originalRequest._retry || isAuthEndpoint) {
    return Promise.reject(error);
  }

  originalRequest._retry = true;
  if (!refreshPromise) refreshPromise = refreshAccessToken().finally(() => { refreshPromise = null; });
  const newAccessToken = await refreshPromise;
  if (!newAccessToken) {
    tokenStore.clear();
    return Promise.reject(error);
  }

  originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
  return api(originalRequest);
});

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = tokenStore.getRefreshToken();
  if (!refreshToken) return null;
  try {
    const response = await axios.post<LoginResponse>(
      `${apiBaseUrl}/auth/refresh`,
      { refreshToken },
    );
    tokenStore.setTokens(response.data.accessToken, response.data.refreshToken);
    return response.data.accessToken;
  } catch {
    return null;
  }
}

export const authApi = {
  login: (request: { email: string; password: string }) => api.post<LoginResponse>('/auth/login', request),
  register: (request: { email: string; password: string; displayName: string; timezone: string }) => api.post('/auth/register', request),
  logout: (refreshToken: string) => api.post('/auth/logout', { refreshToken }),
  verifyEmail: (token: string) => api.post('/auth/verify-email', { token }),
  requestPasswordReset: (email: string) => api.post('/auth/request-password-reset', { email }),
  resetPassword: (token: string, password: string) => api.post('/auth/reset-password', { token, password }),
};

export const userApi = { me: () => api.get('/users/me') };

export const reminderApi = {
  list: (params: { page: number; size: number; completed?: boolean; category?: string; dueFrom?: string; dueTo?: string }) =>
    api.get<ReminderPage>('/reminders', { params }),
  create: (input: ReminderInput) => api.post<Reminder>('/reminders', input),
  update: (id: string, input: ReminderInput) => api.put<Reminder>(`/reminders/${id}`, input),
  setCompleted: (id: string, completed: boolean) => api.patch<Reminder>(`/reminders/${id}/completion`, { completed }),
  delete: (id: string) => api.delete(`/reminders/${id}`),
};

export const notificationApi = {
  list: (page = 0, size = 8) => api.get<NotificationPage>('/notifications', { params: { page, size, sort: 'createdAt,desc' } }),
  unreadCount: () => api.get<{ count: number }>('/notifications/unread-count'),
  markRead: (id: string) => api.patch<Notification>(`/notifications/${id}/read`),
  markAllRead: () => api.patch('/notifications/read-all'),
};

export function getApiErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.') {
  if (axios.isAxiosError(error)) {
    const detail = error.response?.data?.detail;
    if (typeof detail === 'string') return detail;
  }
  return fallback;
}

export default api;
