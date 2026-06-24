import { Image, KeyRound, LogOut, User, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { zodResolver } from '@hookform/resolvers/zod';
import { PasswordInput } from '../auth/PasswordInput';
import { useAuth } from '../../contexts/AuthContext';
import {
  useChangePassword,
  useUpdateAvatar,
  useUpdateUsername,
  useUserInfo,
} from '../../hooks/useUserInfo';
import { ApiError } from '../../lib/api/client';
import {
  changePasswordSchema,
  updateAvatarSchema,
  updateUsernameSchema,
  type ChangePasswordFormData,
} from '../../lib/validations/user';
import { UserAvatar } from './UserAvatar';

type AccountModalProps = {
  open: boolean;
  onClose: () => void;
};

export function AccountModal({ open, onClose }: AccountModalProps) {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const { data: userInfo, isLoading: userLoading } = useUserInfo();
  const updateUsernameMutation = useUpdateUsername();
  const updateAvatarMutation = useUpdateAvatar();
  const changePasswordMutation = useChangePassword();

  const [usernameError, setUsernameError] = useState('');
  const [avatarError, setAvatarError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');

  const usernameForm = useForm<{ username: string }>({
    resolver: zodResolver(updateUsernameSchema),
    defaultValues: { username: '' },
    values: userInfo ? { username: userInfo.username } : undefined,
  });

  const avatarForm = useForm<{ avatarUrl: string }>({
    resolver: zodResolver(updateAvatarSchema),
    defaultValues: { avatarUrl: '' },
    values: userInfo ? { avatarUrl: userInfo.avatarUrl ?? '' } : undefined,
  });

  const passwordForm = useForm<ChangePasswordFormData>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { oldPassword: '', newPassword: '', confirmPassword: '' },
  });

  useEffect(() => {
    if (!open) return;

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [open, onClose]);

  if (!open) {
    return null;
  }

  const onUpdateUsername = async (data: { username: string }) => {
    setUsernameError('');
    try {
      await updateUsernameMutation.mutateAsync(data.username);
    } catch (err) {
      setUsernameError(
        err instanceof ApiError ? err.message : '修改失败，请稍后重试',
      );
    }
  };

  const onUpdateAvatar = async (data: { avatarUrl: string }) => {
    setAvatarError('');
    try {
      await updateAvatarMutation.mutateAsync(data.avatarUrl);
    } catch (err) {
      setAvatarError(
        err instanceof ApiError ? err.message : '修改失败，请稍后重试',
      );
    }
  };

  const onChangePassword = async (data: ChangePasswordFormData) => {
    setPasswordError('');
    setPasswordSuccess('');
    try {
      await changePasswordMutation.mutateAsync({
        oldPassword: data.oldPassword,
        newPassword: data.newPassword,
      });
      setPasswordSuccess('密码修改成功，请重新登录');
      passwordForm.reset();
      setTimeout(() => {
        logout();
        onClose();
        navigate('/login');
      }, 1200);
    } catch (err) {
      setPasswordError(
        err instanceof ApiError ? err.message : '修改失败，请稍后重试',
      );
    }
  };

  const onLogout = () => {
    logout();
    onClose();
    navigate('/login');
  };

  return (
    <div className="account-modal" role="dialog" aria-modal="true" aria-labelledby="account-modal-title">
      <button className="account-modal__scrim" type="button" aria-label="关闭账户面板" onClick={onClose} />
      <section className="account-modal__panel">
        <header className="account-modal__header">
          <div className="account-modal__identity">
            <UserAvatar
              className="account-modal__avatar"
              username={userInfo?.username}
              avatarUrl={userInfo?.avatarUrl}
              decorative
            />
            <div>
              <p className="account-modal__eyebrow">Account</p>
              <h2 id="account-modal-title">{userInfo?.username ?? '账户中心'}</h2>
            </div>
          </div>
          <button className="btn-icon account-modal__close" type="button" aria-label="关闭" onClick={onClose}>
            <X size={17} />
          </button>
        </header>

        <div className="account-modal__body">
          <section className="account-modal__section">
            <h3>基本信息</h3>
            {userLoading ? (
              <p className="account-muted">加载中…</p>
            ) : userInfo ? (
              <dl className="account-modal__info">
                <div>
                  <dt>用户名</dt>
                  <dd>{userInfo.username}</dd>
                </div>
                <div>
                  <dt>手机号</dt>
                  <dd>{userInfo.maskedPhone || '—'}</dd>
                </div>
                <div>
                  <dt>昵称</dt>
                  <dd>{userInfo.nickname}</dd>
                </div>
                <div>
                  <dt>状态</dt>
                  <dd>{userInfo.status}</dd>
                </div>
              </dl>
            ) : (
              <p className="account-muted">无法加载用户信息</p>
            )}
          </section>

          <form className="account-modal__section" onSubmit={usernameForm.handleSubmit(onUpdateUsername)} noValidate>
            <div className="account-modal__section-title">
              <User size={16} />
              <h3>用户名</h3>
            </div>
            <div className={`field${usernameForm.formState.errors.username ? ' has-error' : ''}`}>
              <label htmlFor="modalUsername">新用户名</label>
              <input id="modalUsername" type="text" {...usernameForm.register('username')} />
              {usernameForm.formState.errors.username && (
                <span className="field-error">{usernameForm.formState.errors.username.message}</span>
              )}
            </div>
            {usernameError && <div className="auth-alert" role="alert">{usernameError}</div>}
            <button className="btn primary" type="submit" disabled={updateUsernameMutation.isPending}>
              {updateUsernameMutation.isPending ? '保存中…' : '保存用户名'}
            </button>
          </form>

          <form className="account-modal__section" onSubmit={avatarForm.handleSubmit(onUpdateAvatar)} noValidate>
            <div className="account-modal__section-title">
              <Image size={16} />
              <h3>头像</h3>
            </div>
            <div className={`field${avatarForm.formState.errors.avatarUrl ? ' has-error' : ''}`}>
              <label htmlFor="modalAvatarUrl">头像 URL</label>
              <input
                id="modalAvatarUrl"
                type="url"
                placeholder="https://example.com/avatar.png"
                {...avatarForm.register('avatarUrl')}
              />
              {avatarForm.formState.errors.avatarUrl && (
                <span className="field-error">{avatarForm.formState.errors.avatarUrl.message}</span>
              )}
            </div>
            {avatarError && <div className="auth-alert" role="alert">{avatarError}</div>}
            <button className="btn primary" type="submit" disabled={updateAvatarMutation.isPending}>
              {updateAvatarMutation.isPending ? '保存中…' : '保存头像'}
            </button>
          </form>

          <form className="account-modal__section" onSubmit={passwordForm.handleSubmit(onChangePassword)} noValidate>
            <div className="account-modal__section-title">
              <KeyRound size={16} />
              <h3>密码</h3>
            </div>
            <div className={`field${passwordForm.formState.errors.oldPassword ? ' has-error' : ''}`}>
              <label htmlFor="modalOldPassword">原密码</label>
              <PasswordInput
                id="modalOldPassword"
                autoComplete="current-password"
                hasError={!!passwordForm.formState.errors.oldPassword}
                {...passwordForm.register('oldPassword')}
              />
              {passwordForm.formState.errors.oldPassword && (
                <span className="field-error">{passwordForm.formState.errors.oldPassword.message}</span>
              )}
            </div>
            <div className={`field${passwordForm.formState.errors.newPassword ? ' has-error' : ''}`}>
              <label htmlFor="modalNewPassword">新密码</label>
              <PasswordInput
                id="modalNewPassword"
                autoComplete="new-password"
                hasError={!!passwordForm.formState.errors.newPassword}
                {...passwordForm.register('newPassword')}
              />
              {passwordForm.formState.errors.newPassword && (
                <span className="field-error">{passwordForm.formState.errors.newPassword.message}</span>
              )}
            </div>
            <div className={`field${passwordForm.formState.errors.confirmPassword ? ' has-error' : ''}`}>
              <label htmlFor="modalConfirmPassword">确认新密码</label>
              <PasswordInput
                id="modalConfirmPassword"
                autoComplete="new-password"
                hasError={!!passwordForm.formState.errors.confirmPassword}
                {...passwordForm.register('confirmPassword')}
              />
              {passwordForm.formState.errors.confirmPassword && (
                <span className="field-error">{passwordForm.formState.errors.confirmPassword.message}</span>
              )}
            </div>
            {passwordError && <div className="auth-alert" role="alert">{passwordError}</div>}
            {passwordSuccess && <div className="auth-success" role="status">{passwordSuccess}</div>}
            <button className="btn primary" type="submit" disabled={changePasswordMutation.isPending}>
              {changePasswordMutation.isPending ? '修改中…' : '修改密码'}
            </button>
          </form>
        </div>

        <footer className="account-modal__footer">
          <button className="btn account-logout" type="button" onClick={onLogout}>
            <LogOut size={16} />
            退出登录
          </button>
        </footer>
      </section>
    </div>
  );
}
