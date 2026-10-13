import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi, userApi } from '../api/client';
import { tokenStore } from '../api/tokenStore';
import type { LoginResponse, User } from '../api/types';

type AuthContextValue = { user: User | null; isLoading: boolean; signIn: (response: LoginResponse) => void; signOut: () => Promise<void> };
const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(Boolean(tokenStore.getAccessToken()));

  useEffect(() => {
    if (!tokenStore.getAccessToken()) return;
    userApi.me().then((response) => setUser(response.data)).catch(() => { tokenStore.clear(); setUser(null); }).finally(() => setIsLoading(false));
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isLoading,
    signIn: (response) => { tokenStore.setTokens(response.accessToken, response.refreshToken); setUser(response.user); },
    signOut: async () => {
      const refreshToken = tokenStore.getRefreshToken();
      try { if (refreshToken) await authApi.logout(refreshToken); } finally { tokenStore.clear(); setUser(null); }
    },
  }), [isLoading, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
