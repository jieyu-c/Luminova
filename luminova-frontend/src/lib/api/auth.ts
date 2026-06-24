import { apiFetch } from './client';
import type { LoginResponse } from './types';

export type LoginPayload = {
  phone: string;
  password: string;
};

export function login(data: LoginPayload) {
  return apiFetch<LoginResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify(data),
    skipAuth: true,
  });
}
