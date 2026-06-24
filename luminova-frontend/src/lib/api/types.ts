export type ApiErrorBody = {
  errCode: string;
  errMSg: string;
};

export type BaseResponse = {
  success: boolean;
  error?: ApiErrorBody;
};

export type UserInfo = {
  userId: number;
  username: string;
  nickname: string;
  avatarUrl: string | null;
  remainingCredits?: number;
  status: string;
  maskedPhone: string;
  maskedEmail: string | null;
};

export type LoginResponse = BaseResponse & {
  token?: string;
  userId?: number;
  username?: string;
  avatarUrl?: string | null;
};

export type RegisterResponse = BaseResponse & {
  userInfo?: UserInfo;
};

export type UserInfoResponse = BaseResponse & {
  userInfo?: UserInfo;
};

export type SendVerifyCodeResponse = BaseResponse & {
  expiresInSeconds?: number;
  verifyCode?: string;
};

export type UnauthorizedResponse = {
  code: string;
  message: string;
};
