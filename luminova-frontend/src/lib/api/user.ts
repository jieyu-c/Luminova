import { apiFetch } from './client';
import type {
  BaseResponse,
  RegisterResponse,
  SendVerifyCodeResponse,
  UserInfoResponse,
} from './types';

export function sendVerifyCode(phone: string) {
  return apiFetch<SendVerifyCodeResponse>('/api/v1/user/verify-code/send', {
    method: 'POST',
    body: JSON.stringify({ phone }),
    skipAuth: true,
  });
}

export function register(data: {
  phone: string;
  password: string;
  verifyCode: string;
}) {
  return apiFetch<RegisterResponse>('/api/v1/user/register', {
    method: 'POST',
    body: JSON.stringify(data),
    skipAuth: true,
  });
}

export function getUserInfo() {
  return apiFetch<UserInfoResponse>('/api/v1/user/info');
}

export function updateUsername(username: string) {
  return apiFetch<UserInfoResponse>('/api/v1/user/username', {
    method: 'PUT',
    body: JSON.stringify({ username }),
  });
}

export function updateAvatar(avatarUrl: string) {
  return apiFetch<UserInfoResponse>('/api/v1/user/avatar', {
    method: 'PUT',
    body: JSON.stringify({ avatarUrl }),
  });
}

export function changePassword(oldPassword: string, newPassword: string) {
  return apiFetch<BaseResponse>('/api/v1/user/password', {
    method: 'PUT',
    body: JSON.stringify({ oldPassword, newPassword }),
  });
}
