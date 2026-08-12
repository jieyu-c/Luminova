import { Sparkles } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { LoginForm } from '../components/auth/LoginForm';

export function LoginPage() {
  const navigate = useNavigate();

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

        <div>
          <LoginForm onSuccess={() => navigate('/agent')} />
          <p className="auth-switch">
            还没有账户？
            <Link to="/register">立即注册</Link>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
}
