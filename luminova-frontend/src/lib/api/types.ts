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

export type ProjectType =
  | 'GENERAL'
  | 'SHORT_DRAMA'
  | 'COMIC'
  | 'AD'
  | 'TALKING_HEAD';

export type ProjectStatus =
  | 'DRAFT'
  | 'IN_PROGRESS'
  | 'PENDING_REVIEW'
  | 'COMPLETED';

export type ProjectInfo = {
  projectId: number;
  ownerId: number;
  name: string;
  projectType: ProjectType;
  description?: string | null;
  coverUrl?: string | null;
  aspectRatio?: string | null;
  targetDurationMs?: number | null;
  status: ProjectStatus;
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type ProjectInfoResponse = BaseResponse & {
  projectInfo?: ProjectInfo;
};

export type ProjectPageResponse = BaseResponse & {
  pageNo?: number;
  pageSize?: number;
  total?: number;
  pages?: number;
  records?: ProjectInfo[];
};
