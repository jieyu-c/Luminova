import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  changePassword,
  getUserInfo,
  updateAvatar,
  updateUsername,
} from '../lib/api/user';
import { useAuth } from '../contexts/AuthContext';
import { ApiError } from '../lib/api/client';

export const userInfoQueryKey = ['user', 'info'] as const;

export function useUserInfo() {
  const { isAuthenticated, isLoading, logout } = useAuth();

  return useQuery({
    queryKey: userInfoQueryKey,
    queryFn: async () => {
      try {
        const response = await getUserInfo();
        return response.userInfo ?? null;
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) {
          logout();
        }
        throw err;
      }
    },
    enabled: isAuthenticated && !isLoading,
    retry: (failureCount, error) => {
      if (error instanceof ApiError && error.status === 401) {
        return false;
      }
      return failureCount < 2;
    },
  });
}

export function useUpdateUsername() {
  const queryClient = useQueryClient();
  const { setUser } = useAuth();

  return useMutation({
    mutationFn: (username: string) => updateUsername(username),
    onSuccess: (response) => {
      if (response.userInfo) {
        setUser(response.userInfo);
        queryClient.setQueryData(userInfoQueryKey, response.userInfo);
      }
    },
  });
}

export function useUpdateAvatar() {
  const queryClient = useQueryClient();
  const { setUser } = useAuth();

  return useMutation({
    mutationFn: (avatarUrl: string) => updateAvatar(avatarUrl),
    onSuccess: (response) => {
      if (response.userInfo) {
        setUser(response.userInfo);
        queryClient.setQueryData(userInfoQueryKey, response.userInfo);
      }
    },
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: ({
      oldPassword,
      newPassword,
    }: {
      oldPassword: string;
      newPassword: string;
    }) => changePassword(oldPassword, newPassword),
  });
}
