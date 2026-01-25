'use client';

const API_URL = 'http://localhost:8080/api';

export const getAuthToken = () => {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('token');
  }
  return null;
};

export const setAuthToken = (token: string) => {
  if (typeof window !== 'undefined') {
    localStorage.setItem('token', token);
  }
};

export const removeAuthToken = () => {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('token');
  }
};

interface FetchOptions extends RequestInit {
  headers?: Record<string, string>;
}

export const apiFetch = async (endpoint: string, options: FetchOptions = {}) => {
  const token = getAuthToken();
  
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    // Handle unauthorized (logout)
    removeAuthToken();
    if (window.location.pathname !== '/auth/login' && window.location.pathname !== '/auth/register') {
      window.location.href = '/auth/login';
    }
  }

  const data = await response.json().catch(() => ({}));
  
  if (!response.ok) {
    throw new Error(data.message || 'Something went wrong');
  }

  return data;
};
