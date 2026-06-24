import { zodResolver } from '@hookform/resolvers/zod';
import { Sparkles } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { PasswordInput } from '../components/auth/PasswordInput';
import { register as registerUser, sendVerifyCode } from '../lib/api/user';
import { ApiError } from '../lib/api/client';
import {
  registerSchema,
  type RegisterFormData,
} from '../lib/validations/auth';

const COOLDOWN_SECONDS = 60;

export function RegisterPage() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState('');
  const [cooldown, setCooldown] = useState(0);
  const [devVerifyCode, setDevVerifyCode] = useState('');

  const {
    register,
    handleSubmit,
    getValues,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: { phone: '', verifyCode: '', password: '', confirmPassword: '' },
  });

  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = window.setTimeout(() => setCooldown((c) => c - 1), 1000);
    return () => window.clearTimeout(timer);
  }, [cooldown]);

  const handleSendCode = useCallback(async () => {
    setServerError('');
    const phone = getValues('phone');
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      setServerError('请先输入有效的手机号');
      return;
    }
    try {
      const response = await sendVerifyCode(phone);
      setCooldown(COOLDOWN_SECONDS);
      if (response.verifyCode) {
        setDevVerifyCode(response.verifyCode);
        setValue('verifyCode', response.verifyCode);
      }
    } catch (err) {
      setServerError(
        err instanceof ApiError ? err.message : '验证码发送失败，请稍后重试',
      );
    }
  }, [getValues, setValue]);

  const onSubmit = async (data: RegisterFormData) => {
    setServerError('');
    try {
      const { confirmPassword: _, ...payload } = data;
      await registerUser(payload);
      navigate('/login');
    } catch (err) {
      setServerError(
        err instanceof ApiError ? err.message : '注册失败，请稍后重试',
      );
    }
  };

  return (
    <AuthLayout>
      <div className="auth-card glass-gradient">
        <aside className="auth-aside">
          <span className="eyebrow">
            <Sparkles aria-hidden="true" />
            Get Started
          </span>
          <h1>
            注册
            <span>开启 AI 视频创作新体验</span>
          </h1>
          <p className="auth-aside-lead">
            创建 Luminova 账户，体验从创意到成片的 AI 驱动工作流。注册即可免费探索核心功能。
          </p>
          <ul className="auth-features">
            <li>
              <b>免费体验</b>
              <span>注册即可使用基础创作工具</span>
            </li>
            <li>
              <b>云端同步</b>
              <span>项目与画布数据安全存储</span>
            </li>
            <li>
              <b>持续更新</b>
              <span>AI 模型与功能持续迭代</span>
            </li>
          </ul>
        </aside>

        <section className="auth-form-panel">
          <div className="auth-form-head">
            <h2>创建账户</h2>
            <p>使用手机号注册你的 Luminova 账户</p>
          </div>

          <form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <div className={`field${errors.phone ? ' has-error' : ''}`}>
              <label htmlFor="phone">手机号</label>
              <input
                id="phone"
                type="tel"
                inputMode="numeric"
                autoComplete="tel"
                placeholder="请输入 11 位手机号"
                maxLength={11}
                {...register('phone')}
              />
              {errors.phone && (
                <span className="field-error">{errors.phone.message}</span>
              )}
            </div>

            <div className={`field${errors.verifyCode ? ' has-error' : ''}`}>
              <label htmlFor="verifyCode">验证码</label>
              <div className="verify-code-row">
                <input
                  id="verifyCode"
                  type="text"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  placeholder="6 位验证码"
                  maxLength={6}
                  {...register('verifyCode')}
                />
                <button
                  className="btn verify-code-btn"
                  type="button"
                  disabled={cooldown > 0}
                  onClick={handleSendCode}
                >
                  {cooldown > 0 ? `${cooldown}s 后重发` : '获取验证码'}
                </button>
              </div>
              {devVerifyCode && (
                <span className="field-hint">开发环境验证码：{devVerifyCode}</span>
              )}
              {errors.verifyCode && (
                <span className="field-error">{errors.verifyCode.message}</span>
              )}
            </div>

            <div className={`field${errors.password ? ' has-error' : ''}`}>
              <label htmlFor="password">密码</label>
              <PasswordInput
                id="password"
                autoComplete="new-password"
                placeholder="8–20 位，含字母和数字"
                hasError={!!errors.password}
                {...register('password')}
              />
              {errors.password && (
                <span className="field-error">{errors.password.message}</span>
              )}
            </div>

            <div className={`field${errors.confirmPassword ? ' has-error' : ''}`}>
              <label htmlFor="confirmPassword">确认密码</label>
              <PasswordInput
                id="confirmPassword"
                autoComplete="new-password"
                placeholder="再次输入密码"
                hasError={!!errors.confirmPassword}
                {...register('confirmPassword')}
              />
              {errors.confirmPassword && (
                <span className="field-error">
                  {errors.confirmPassword.message}
                </span>
              )}
            </div>

            {serverError && (
              <div className="auth-alert" role="alert">
                {serverError}
              </div>
            )}

            <button
              className="btn primary auth-submit"
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? '注册中…' : '注册'}
            </button>
          </form>

          <p className="auth-switch">
            已有账户？
            <Link to="/login">立即登录</Link>
          </p>
        </section>
      </div>
    </AuthLayout>
  );
}
