'use client';

import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth interceptor
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// User Service APIs
export const userApi = {
  login: async (email: string, password: string) =>
    api.post('/api/auth/login', { email, password }),
  signup: async (userData: any) =>
    api.post('/api/auth/signup', userData),
  getCurrentUser: async () =>
    api.get('/api/users/me'),
};

// Transaction Service APIs
export const transactionApi = {
  createTransaction: async (transaction: any) =>
    // backend POST endpoint is /api/transactions/create
    api.post('/api/transactions/create', transaction),
  getTransactions: async () =>
    api.get('/api/transactions/all'),
  // transaction service currently exposes only /all and /create; no single-get implemented
  getTransaction: async (id: string) =>
    api.get(`/api/transactions/${id}`),
};

// Reward Service APIs
export const rewardApi = {
  getAllRewards: async () => api.get('/api/rewards/'),
  getRewardsByUserId: async (userId: number | string) => api.get(`/api/rewards/user/${userId}`),
};

export default api;
