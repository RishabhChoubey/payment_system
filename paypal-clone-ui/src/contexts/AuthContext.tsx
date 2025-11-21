'use client';

import { createContext, useContext, useEffect, useState } from 'react';
import api, { userApi } from '@/lib/api';

interface AuthContextType {
  user: any;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
      if (token) {
        // Ensure axios default header is set for immediate requests
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        const { data } = await userApi.getCurrentUser();
        setUser(data);
      }
    } catch (error) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
      }
    } finally {
      setLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    const { data } = await userApi.login(email, password);
    console.log(data)
    if (typeof window !== 'undefined') {
      localStorage.setItem('token', data.token);
      // Also set axios default Authorization header immediately so subsequent
      // requests made in the same tick (like getCurrentUser) include it.
      api.defaults.headers.common['Authorization'] = `Bearer ${data.token}`;
    }
    // Backend currently returns only the token. Fetch the current user using the
    // stored token (api interceptor will attach it) and set it in context.
    try {
      const { data: userData } = await userApi.getCurrentUser();
      console.log(userData)
      setUser(userData);
    } catch (err) {
      // If fetching the user fails, clear token and keep user as null.
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
      }
      setUser(null);
      throw err; // rethrow so callers (login page) can show error
    }
  };

  const logout = () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('token');
    }
    // Remove default header as well
    try {
      delete api.defaults.headers.common['Authorization'];
    } catch (e) {
      // ignore in environments where api is not available
    }
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

// Export the useAuth hook
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
