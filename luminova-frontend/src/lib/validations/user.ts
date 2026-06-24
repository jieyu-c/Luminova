import { z } from 'zod';
import { passwordSchema } from './auth';

export const usernameSchema = z
  .string()
  .min(1, '请输入用户名')
  .regex(
    /^[a-zA-Z][a-zA-Z0-9_]{3,31}$/,
    '用户名需以字母开头，4–32 位字母、数字或下划线',
  );

export const avatarUrlSchema = z
  .string()
  .min(1, '请输入头像 URL')
  .max(512, 'URL 过长')
  .regex(/^https?:\/\/\S+$/, '请输入有效的 http 或 https URL');

export const changePasswordSchema = z
  .object({
    oldPassword: z.string().min(1, '请输入原密码'),
    newPassword: passwordSchema,
    confirmPassword: z.string().min(1, '请确认新密码'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: '两次输入的新密码不一致',
    path: ['confirmPassword'],
  })
  .refine((data) => data.oldPassword !== data.newPassword, {
    message: '新密码不能与原密码相同',
    path: ['newPassword'],
  });

export type ChangePasswordFormData = z.infer<typeof changePasswordSchema>;

export const updateUsernameSchema = z.object({
  username: usernameSchema,
});

export const updateAvatarSchema = z.object({
  avatarUrl: avatarUrlSchema,
});

export type UpdateUsernameFormData = z.infer<typeof updateUsernameSchema>;
export type UpdateAvatarFormData = z.infer<typeof updateAvatarSchema>;
