import { Link } from 'react-router-dom';
import type { ReactNode } from 'react';
import { LuminovaMark } from '../brand/LuminovaMark';

type AuthLayoutProps = {
  children: ReactNode;
};

export function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="site-shell">
      <div className="ambient" aria-hidden="true" />
      <header className="auth-nav">
        <Link className="brand" to="/" aria-label="Luminova 首页">
          <LuminovaMark className="brand-mark" />
          <span className="brand-word">
            <b>Luminova</b>
            <strong>·</strong>
            <em>灵衍</em>
          </span>
        </Link>
        <Link className="btn" to="/">
          返回首页
        </Link>
      </header>
      <main className="auth-page">{children}</main>
    </div>
  );
}
