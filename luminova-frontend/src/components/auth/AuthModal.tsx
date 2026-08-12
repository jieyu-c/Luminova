import { Sparkles, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';

export type AuthMode = 'login' | 'register';

type AuthModalProps = {
  open: boolean;
  mode: AuthMode;
  onModeChange: (mode: AuthMode) => void;
  onClose: () => void;
  onAuthenticated?: () => void;
};

const authCopy = {
  login: {
    eyebrow: 'Welcome Back',
    title: '登录',
    subtitle: '继续你的 AI 创作之旅',
    lead: '使用手机号登录 Luminova，进入工作空间与画布，让 AI 帮你完成从脚本到成片的完整流程。',
    features: [
      ['Generation Graph', '节点式 AI 工作流编排'],
      ['Multi-Agent', '多智能体协同创作'],
      ['Canvas', '可视化编辑与实时预览'],
    ],
  },
  register: {
    eyebrow: 'Get Started',
    title: '注册',
    subtitle: '开启 AI 视频创作新体验',
    lead: '创建 Luminova 账户，体验从创意到成片的 AI 驱动工作流。注册即可免费探索核心功能。',
    features: [
      ['免费体验', '注册即可使用基础创作工具'],
      ['云端同步', '项目与画布数据安全存储'],
      ['持续更新', 'AI 模型与功能持续迭代'],
    ],
  },
} as const;

export function AuthModal({
  open,
  mode,
  onModeChange,
  onClose,
  onAuthenticated,
}: AuthModalProps) {
  const [registerSuccess, setRegisterSuccess] = useState('');
  const copy = authCopy[mode];

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

  useEffect(() => {
    if (open) return;
    setRegisterSuccess('');
  }, [open]);

  if (!open) {
    return null;
  }

  const switchMode = (nextMode: AuthMode) => {
    setRegisterSuccess('');
    onModeChange(nextMode);
  };

  return (
    <div
      className="auth-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="auth-modal-title"
    >
      <button
        className="auth-modal__scrim"
        type="button"
        aria-label="关闭登录注册弹窗"
        onClick={onClose}
      />
      <div className="auth-modal__panel auth-card glass-gradient">
        <aside className="auth-aside auth-modal__aside">
          <span className="eyebrow">
            <Sparkles aria-hidden="true" />
            {copy.eyebrow}
          </span>
          <h1 id="auth-modal-title">
            {copy.title}
            <span>{copy.subtitle}</span>
          </h1>
          <p className="auth-aside-lead">{copy.lead}</p>
          <ul className="auth-features">
            {copy.features.map(([title, description]) => (
              <li key={title}>
                <b>{title}</b>
                <span>{description}</span>
              </li>
            ))}
          </ul>
        </aside>

        <div className="auth-modal__form-wrap">
          <button
            className="btn-icon auth-modal__close"
            type="button"
            aria-label="关闭"
            onClick={onClose}
          >
            <X size={17} />
          </button>
          {registerSuccess ? (
            <div className="auth-success auth-modal__success" role="status">
              {registerSuccess}
            </div>
          ) : null}
          {mode === 'login' ? (
            <LoginForm
              idPrefix="auth-modal-login"
              onSuccess={() => {
                onClose();
                onAuthenticated?.();
              }}
              onSwitchToRegister={() => switchMode('register')}
            />
          ) : (
            <RegisterForm
              idPrefix="auth-modal-register"
              onSuccess={() => {
                setRegisterSuccess('注册成功，请继续登录');
                onModeChange('login');
              }}
              onSwitchToLogin={() => switchMode('login')}
            />
          )}
        </div>
      </div>
    </div>
  );
}
