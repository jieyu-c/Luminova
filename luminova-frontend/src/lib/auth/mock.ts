import { ApiError } from '../api/client';

export const MOCK_ACCOUNT = 'luminova';
export const MOCK_PASSWORD = 'admin-password';

export async function mockLogin(account: string, password: string) {
  await new Promise((resolve) => setTimeout(resolve, 400));

  if (account === MOCK_ACCOUNT && password === MOCK_PASSWORD) {
    return {
      token: 'mock-token-luminova',
      user: {
        id: 'mock-user-1',
        phone: MOCK_ACCOUNT,
      },
    };
  }

  throw new ApiError('账号或密码错误', 401);
}
