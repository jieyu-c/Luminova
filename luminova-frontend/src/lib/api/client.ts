import { resolveApiUrl } from '../config/env';
import { getToken } from '../auth/session';
import type { BaseResponse, UnauthorizedResponse } from './types';

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public code?: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

type ApiFetchOptions = RequestInit & {
  skipAuth?: boolean;
};

export async function apiFetch<T extends BaseResponse>(
  path: string,
  options: ApiFetchOptions = {},
): Promise<T> {
  const { skipAuth = false, headers: customHeaders, ...rest } = options;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(customHeaders as Record<string, string>),
  };

  if (!skipAuth) {
    const token = getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(resolveApiUrl(path), {
    ...rest,
    headers,
  });

  if (response.status === 401) {
    let message = '未登录，请重新登录';
    try {
      const body = (await response.json()) as UnauthorizedResponse;
      if (body.message) message = body.message;
    } catch {
      // ignore
    }
    throw new ApiError(message, 401, 'UNAUTHORIZED');
  }

  if (response.status === 204) {
    return { success: true } as T;
  }

  let body: T;
  try {
    body = (await response.json()) as T;
  } catch {
    throw new ApiError('请求失败，请稍后重试', response.status);
  }

  if (!response.ok) {
    const message = body.error?.errMSg ?? '请求失败，请稍后重试';
    throw new ApiError(message, response.status, body.error?.errCode);
  }

  if (body.success === false) {
    const message = body.error?.errMSg ?? '操作失败，请稍后重试';
    throw new ApiError(message, response.status, body.error?.errCode);
  }

  return body;
}
