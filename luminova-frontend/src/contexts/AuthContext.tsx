import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { getUserInfo } from '../lib/api/user';
import {
  clearToken,
  getToken,
  isAuthenticated,
  setToken,
} from '../lib/auth/session';
import { ApiError } from '../lib/api/client';
import type { UserInfo } from '../lib/api/types';

type AuthContextValue = {
  isAuthenticated: boolean;
  user: UserInfo | null;
  isLoading: boolean;
  login: (token: string, user: Partial<UserInfo>) => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
  setUser: (user: UserInfo | null) => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authenticated, setAuthenticated] = useState(isAuthenticated);
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(Boolean(getToken()));

  const refreshUser = useCallback(async () => {
    if (!getToken()) {
      setUser(null);
      setAuthenticated(false);
      return;
    }
    try {
      const response = await getUserInfo();
      if (response.userInfo) {
        setUser(response.userInfo);
        setAuthenticated(true);
      }
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        clearToken();
        setUser(null);
        setAuthenticated(false);
      }
    }
  }, []);

  useEffect(() => {
    if (getToken()) {
      refreshUser().finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, [refreshUser]);

  const login = useCallback((token: string, partialUser: Partial<UserInfo>) => {
    setToken(token);
    setAuthenticated(true);
    if (partialUser.userId) {
      setUser((prev) => ({
        userId: partialUser.userId!,
        username: partialUser.username ?? prev?.username ?? '',
        nickname: partialUser.nickname ?? prev?.nickname ?? partialUser.username ?? '',
        avatarUrl: partialUser.avatarUrl ?? prev?.avatarUrl ?? null,
        remainingCredits: partialUser.remainingCredits ?? prev?.remainingCredits,
        status: partialUser.status ?? prev?.status ?? 'NORMAL',
        maskedPhone: partialUser.maskedPhone ?? prev?.maskedPhone ?? '',
        maskedEmail: partialUser.maskedEmail ?? prev?.maskedEmail ?? null,
      }));
    }
  }, []);

  const logout = useCallback(() => {
    clearToken();
    setAuthenticated(false);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      isAuthenticated: authenticated,
      user,
      isLoading,
      login,
      logout,
      refreshUser,
      setUser,
    }),
    [authenticated, user, isLoading, login, logout, refreshUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
