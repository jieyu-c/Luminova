import { z } from 'zod';

export const phoneSchema = z
  .string()
  .min(1, '请输入手机号')
  .regex(/^1[3-9]\d{9}$/, '请输入有效的 11 位手机号');

export const passwordSchema = z
  .string()
  .min(1, '请输入密码')
  .min(8, '密码至少 8 位')
  .max(20, '密码最多 20 位')
  .regex(
    /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d.@$!%*?&]{8,20}$/,
    '密码需包含字母和数字',
  );

export const verifyCodeSchema = z
  .string()
  .min(1, '请输入验证码')
  .regex(/^\d{6}$/, '验证码为 6 位数字');

export const loginSchema = z.object({
  phone: phoneSchema,
  password: passwordSchema,
});

export const registerSchema = z
  .object({
    phone: phoneSchema,
    verifyCode: verifyCodeSchema,
    password: passwordSchema,
    confirmPassword: z.string().min(1, '请确认密码'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: '两次输入的密码不一致',
    path: ['confirmPassword'],
  });

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
