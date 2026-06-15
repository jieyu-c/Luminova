import { z } from 'zod';

const phoneSchema = z
  .string()
  .min(1, '请输入手机号')
  .regex(/^1[3-9]\d{9}$/, '请输入有效的 11 位手机号');

const passwordSchema = z
  .string()
  .min(1, '请输入密码')
  .min(6, '密码至少 6 位')
  .max(32, '密码最多 32 位');

export const loginSchema = z.object({
  account: z.string().min(1, '请输入账号'),
  password: passwordSchema,
});

export const registerSchema = z
  .object({
    phone: phoneSchema,
    password: passwordSchema,
    confirmPassword: z.string().min(1, '请确认密码'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: '两次输入的密码不一致',
    path: ['confirmPassword'],
  });

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
