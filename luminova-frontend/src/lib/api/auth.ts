import { mockLogin } from '../auth/mock';
import { apiFetch } from './client';
import type { LoginFormData, RegisterFormData } from '../validations/auth';

export type AuthResponse = {
  token: string;
  user?: {
    id: string;
    phone: string;
  };
};

export function login(data: LoginFormData) {
  return mockLogin(data.account, data.password);
}

export function register(data: Omit<RegisterFormData, 'confirmPassword'>) {
  return apiFetch<AuthResponse>('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
