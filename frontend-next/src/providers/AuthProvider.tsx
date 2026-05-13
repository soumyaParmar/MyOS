'use client';

import React, { createContext, useContext, useEffect, useState, useCallback, useMemo } from 'react';
import { isLoggedIn, clearLoginState } from '@/lib/token';
import api from '@/lib/api';
import { authService } from '@/services/auth.service';

interface User {
  id: string;
  name: string;
  email: string;
  roles: string[];
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  logout: () => void;
  checkAuth: () => Promise<void>;
  setUser: (user: User | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode, initialUser?: User | null }> = ({ children, initialUser = null }) => {
  const [user, setUser] = useState<User | null>(initialUser);
  const [isLoading, setIsLoading] = useState(!initialUser);

  const checkAuth = useCallback(async () => {
    // If not flagged as logged in, don't even bother
    if (!isLoggedIn()) {
      setUser(null);
      setIsLoading(false);
      return;
    }

    try {
      // Fetch current user profile - interceptor will handle refresh if access token is expired
      const response = await api.get('/users/me');
      const userData = response.data;
      
      setUser(prevUser => {
        const newUser = {
          ...userData,
          roles: Array.isArray(userData.roles) ? userData.roles : userData.roles ? userData.roles.split(',') : []
        };
        // Simple equality check to prevent unnecessary updates
        if (prevUser && prevUser.id === newUser.id && JSON.stringify(prevUser.roles) === JSON.stringify(newUser.roles)) {
          return prevUser;
        }
        return newUser;
      });
    } catch (error) {
      console.error('Auth verification failed:', error);
      clearLoginState();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout failed on backend:', error);
    } finally {
      clearLoginState();
      setUser(null);
      window.location.href = '/login';
    }
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  const contextValue = useMemo(() => ({
    user,
    isAuthenticated: !!user,
    isLoading,
    logout,
    checkAuth,
    setUser,
  }), [user, isLoading, logout, checkAuth]);

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
