import { zodResolver } from '@hookform/resolvers/zod';
import { Sparkles } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { PasswordInput } from '../components/auth/PasswordInput';
import { useAuth } from '../contexts/AuthContext';
import { login } from '../lib/api/auth';
import { ApiError } from '../lib/api/client';
import { loginSchema, type LoginFormData } from '../lib/validations/auth';

export function LoginPage() {
  const navigate = useNavigate();
  const { login: setAuth } = useAuth();
  const [serverError, setServerError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: { phone: '', password: '' },
  });

  const onSubmit = async (data: LoginFormData) => {
    setServerError('');
    try {
      const response = await login(data);
      if (!response.token || !response.userId) {
        throw new ApiError('登录失败，请稍后重试', 200);
      }
      setAuth(response.token, {
        userId: response.userId,
        username: response.username,
        avatarUrl: response.avatarUrl ?? null,
      });
      navigate('/agent');
    } catch (err) {
      setServerError(
        err instanceof ApiError ? err.message : '登录失败，请稍后重试',
      );
    }
  };

  return (
    <AuthLayout>
      <div className="auth-card glass-gradient">
        <aside className="auth-aside">
          <span className="eyebrow">
            <Sparkles aria-hidden="true" />
            Welcome Back
          </span>
          <h1>
            登录
            <span>继续你的 AI 创作之旅</span>
          </h1>
          <p className="auth-aside-lead">
            使用手机号登录 Luminova，进入工作空间与画布，让 AI 帮你完成从脚本到成片的完整流程。
          </p>
          <ul className="auth-features">
            <li>
              <b>Generation Graph</b>
              <span>节点式 AI 工作流编排</span>
            </li>
            <li>
              <b>Multi-Agent</b>
              <span>多智能体协同创作</span>
            </li>
            <li>
              <b>Canvas</b>
              <span>可视化编辑与实时预览</span>
            </li>
          </ul>
        </aside>

        <section className="auth-form-panel">
          <div className="auth-form-head">
            <h2>账号登录</h2>
            <p>使用手机号和密码登录你的账户</p>
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

            <div className={`field${errors.password ? ' has-error' : ''}`}>
              <label htmlFor="password">密码</label>
              <PasswordInput
                id="password"
                autoComplete="current-password"
                hasError={!!errors.password}
                {...register('password')}
              />
              {errors.password && (
                <span className="field-error">{errors.password.message}</span>
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
              {isSubmitting ? '登录中…' : '登录'}
            </button>
          </form>

          <p className="auth-switch">
            还没有账户？
            <Link to="/register">立即注册</Link>
          </p>
        </section>
      </div>
    </AuthLayout>
  );
}
