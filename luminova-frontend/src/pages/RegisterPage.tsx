import { Sparkles } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { RegisterForm } from '../components/auth/RegisterForm';

export function RegisterPage() {
  const navigate = useNavigate();

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

        <div>
          <RegisterForm onSuccess={() => navigate('/login')} />
          <p className="auth-switch">
            已有账户？
            <Link to="/login">立即登录</Link>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
}
