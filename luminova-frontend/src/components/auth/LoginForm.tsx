import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { PasswordInput } from './PasswordInput';
import { useAuth } from '../../contexts/AuthContext';
import { login } from '../../lib/api/auth';
import { ApiError } from '../../lib/api/client';
import { loginSchema, type LoginFormData } from '../../lib/validations/auth';

type LoginFormProps = {
  idPrefix?: string;
  onSuccess?: () => void;
  onSwitchToRegister?: () => void;
};

export function LoginForm({
  idPrefix = 'login',
  onSuccess,
  onSwitchToRegister,
}: LoginFormProps) {
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
      onSuccess?.();
    } catch (err) {
      setServerError(
        err instanceof ApiError ? err.message : '登录失败，请稍后重试',
      );
    }
  };

  return (
    <section className="auth-form-panel">
      <div className="auth-form-head">
        <h2>账号登录</h2>
        <p>使用手机号和密码登录你的账户</p>
      </div>

      <form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className={`field${errors.phone ? ' has-error' : ''}`}>
          <label htmlFor={`${idPrefix}-phone`}>手机号</label>
          <input
            id={`${idPrefix}-phone`}
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
          <label htmlFor={`${idPrefix}-password`}>密码</label>
          <PasswordInput
            id={`${idPrefix}-password`}
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

      {onSwitchToRegister ? (
        <p className="auth-switch">
          还没有账户？
          <button type="button" onClick={onSwitchToRegister}>
            立即注册
          </button>
        </p>
      ) : null}
    </section>
  );
}
