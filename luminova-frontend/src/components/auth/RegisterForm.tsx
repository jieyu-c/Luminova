import { zodResolver } from '@hookform/resolvers/zod';
import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { PasswordInput } from './PasswordInput';
import { register as registerUser, sendVerifyCode } from '../../lib/api/user';
import { ApiError } from '../../lib/api/client';
import {
  registerSchema,
  type RegisterFormData,
} from '../../lib/validations/auth';

const COOLDOWN_SECONDS = 60;

type RegisterFormProps = {
  idPrefix?: string;
  onSuccess?: () => void;
  onSwitchToLogin?: () => void;
};

export function RegisterForm({
  idPrefix = 'register',
  onSuccess,
  onSwitchToLogin,
}: RegisterFormProps) {
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
      await registerUser({
        phone: data.phone,
        password: data.password,
        verifyCode: data.verifyCode,
      });
      onSuccess?.();
    } catch (err) {
      setServerError(
        err instanceof ApiError ? err.message : '注册失败，请稍后重试',
      );
    }
  };

  return (
    <section className="auth-form-panel">
      <div className="auth-form-head">
        <h2>创建账户</h2>
        <p>使用手机号注册你的 Luminova 账户</p>
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

        <div className={`field${errors.verifyCode ? ' has-error' : ''}`}>
          <label htmlFor={`${idPrefix}-verifyCode`}>验证码</label>
          <div className="verify-code-row">
            <input
              id={`${idPrefix}-verifyCode`}
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
          <label htmlFor={`${idPrefix}-password`}>密码</label>
          <PasswordInput
            id={`${idPrefix}-password`}
            autoComplete="new-password"
            placeholder="8-20 位，含字母和数字"
            hasError={!!errors.password}
            {...register('password')}
          />
          {errors.password && (
            <span className="field-error">{errors.password.message}</span>
          )}
        </div>

        <div className={`field${errors.confirmPassword ? ' has-error' : ''}`}>
          <label htmlFor={`${idPrefix}-confirmPassword`}>确认密码</label>
          <PasswordInput
            id={`${idPrefix}-confirmPassword`}
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

      {onSwitchToLogin ? (
        <p className="auth-switch">
          已有账户？
          <button type="button" onClick={onSwitchToLogin}>
            立即登录
          </button>
        </p>
      ) : null}
    </section>
  );
}
