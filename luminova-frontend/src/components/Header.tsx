import { Link, useLocation } from 'react-router-dom';
import { brandAssets } from '../data/brand';
import { useAuth } from '../contexts/AuthContext';
import { cn } from '../lib/cn';

const marketingNav = [
  { href: '#product', label: '产品' },
  { href: '#workflow', label: '工作流' },
  { href: '#cases', label: '案例' },
  { href: '#pricing', label: '定价' },
] as const;

export function Header() {
  const { isAuthenticated, logout } = useAuth();
  const { pathname } = useLocation();
  const isHome = pathname === '/';
  const isWorkspace = pathname === '/workspace';
  const isCanvas = pathname === '/canvas';

  if (isHome) {
    return (
      <header className="site-header">
        <Link className="brand" to="/" aria-label="Luminova 灵衍首页">
          <img className="brand-mark" src={brandAssets.logoMark} alt="" width={40} height={40} />
          <span className="brand-name">
            Luminova <em>灵衍</em>
          </span>
        </Link>
        <nav className="site-nav" aria-label="官网导航">
          {marketingNav.map((item) => (
            <a href={item.href} key={item.href}>
              {item.label}
            </a>
          ))}
        </nav>
        <div className="site-actions">
          {isAuthenticated ? (
            <>
              <button className="btn btn-quiet" type="button" onClick={logout}>
                退出登录
              </button>
              <Link className="btn btn-primary" to="/workspace">
                开始创作
              </Link>
            </>
          ) : (
            <>
              <Link className="btn btn-quiet" to="/login">
                登录
              </Link>
              <Link className="btn btn-primary" to="/register">
                注册
              </Link>
            </>
          )}
        </div>
      </header>
    );
  }

  return (
    <header className="shell-nav">
      <Link className="brand" to="/" aria-label="Luminova 首页">
        <img className="brand-mark" src={brandAssets.logoMark} alt="" width={40} height={40} />
        <span className="brand-word">
          <b>Luminova</b>
          <strong>·</strong>
          <em>灵衍</em>
        </span>
      </Link>
      <nav className="nav" aria-label="主导航">
        <Link className={cn(pathname === '/' && 'active')} to="/">
          首页
        </Link>
        <Link
          className={cn(isWorkspace && 'active')}
          to={isAuthenticated ? '/workspace' : '/login'}
        >
          工作空间
        </Link>
        <Link className={cn(isCanvas && 'active')} to={isAuthenticated ? '/canvas' : '/login'}>
          画布
        </Link>
      </nav>
      <div className="actions">
        {isWorkspace ? (
          <>
            <button className="btn ghost-ai" type="button">
              Agent 日志
            </button>
            <Link className="btn primary" to="/canvas">
              新建项目
            </Link>
          </>
        ) : isAuthenticated ? (
          <>
            <button className="btn" type="button" onClick={logout}>
              退出登录
            </button>
            <Link className="btn primary" to="/workspace">
              开始创作
            </Link>
          </>
        ) : (
          <>
            <Link className="btn" to="/login">
              登录
            </Link>
            <Link className="btn primary" to="/register">
              注册
            </Link>
          </>
        )}
      </div>
    </header>
  );
}
