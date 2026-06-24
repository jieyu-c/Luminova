import { Link, useLocation } from 'react-router-dom';
import { useState } from 'react';
import { Sparkles } from 'lucide-react';
import { AccountModal } from './account/AccountModal';
import { UserAvatar } from './account/UserAvatar';
import { brandAssets } from '../data/brand';
import { useAuth } from '../contexts/AuthContext';
import { cn } from '../lib/cn';

const marketingNav = [
  { href: '#product', label: '产品' },
  { href: '#workflow', label: '工作流' },
  { href: '#cases', label: '案例' },
  { href: '#pricing', label: '定价' },
] as const;

const DEMO_REMAINING_CREDITS = 1280;

function CreditsBalance() {
  const { user } = useAuth();
  const remainingCredits = user?.remainingCredits ?? DEMO_REMAINING_CREDITS;

  return (
    <div
      className="header-credits"
      title={`当前剩余 ${remainingCredits.toLocaleString('zh-CN')} 积分`}
      aria-label={`当前剩余积分 ${remainingCredits.toLocaleString('zh-CN')}`}
    >
      <span className="header-credits__icon" aria-hidden="true">
        <Sparkles size={15} strokeWidth={2.2} />
      </span>
      <span className="header-credits__copy">
        <span className="header-credits__label">剩余积分</span>
        <strong>{remainingCredits.toLocaleString('zh-CN')}</strong>
      </span>
    </div>
  );
}

function AccountButton({ onOpen }: { onOpen: () => void }) {
  const { user } = useAuth();

  return (
    <button className="btn btn-quiet account-link" type="button" onClick={onOpen}>
      {user?.avatarUrl ? (
        <img className="account-link-avatar" src={user.avatarUrl} alt="" />
      ) : null}
      <span>{user?.username ?? '账户'}</span>
    </button>
  );
}

function ShellAccountButton({ onOpen }: { onOpen: () => void }) {
  const { user } = useAuth();

  return (
    <button
      className="workspace-account-button"
      type="button"
      title="个人中心"
      aria-label={`打开${user?.username ?? '用户'}的个人中心`}
      onClick={onOpen}
    >
      <UserAvatar
        className="workspace-account-button__avatar"
        username={user?.username}
        avatarUrl={user?.avatarUrl}
        decorative
      />
    </button>
  );
}

export function Header() {
  const { isAuthenticated } = useAuth();
  const [isAccountOpen, setIsAccountOpen] = useState(false);
  const { pathname } = useLocation();
  const isHome = pathname === '/';
  const isAgent = pathname === '/agent';
  const isWorkspace = pathname === '/workspace';

  if (isHome) {
    return (
      <>
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
                <CreditsBalance />
                <AccountButton onOpen={() => setIsAccountOpen(true)} />
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
        <AccountModal open={isAccountOpen} onClose={() => setIsAccountOpen(false)} />
      </>
    );
  }

  return (
    <>
      <header className="shell-nav">
        <Link className="brand" to={isAuthenticated ? '/agent' : '/'} aria-label="Luminova 首页">
          <img className="brand-mark" src={brandAssets.logoMark} alt="" width={40} height={40} />
          <span className="brand-word">
            <b>Luminova</b>
            <strong>·</strong>
            <em>灵衍</em>
          </span>
        </Link>
        <nav className="nav" aria-label="主导航">
          <Link className={cn(isAgent && 'active')} to={isAuthenticated ? '/agent' : '/login'}>
            Agent
          </Link>
          <Link
            className={cn(isWorkspace && 'active')}
            to={isAuthenticated ? '/workspace' : '/login'}
          >
            工作空间
          </Link>
        </nav>
        <div className="actions">
          {isAuthenticated && (isAgent || isWorkspace) ? (
            <>
              <CreditsBalance />
              <ShellAccountButton onOpen={() => setIsAccountOpen(true)} />
            </>
          ) : isAuthenticated ? (
            <>
              <CreditsBalance />
              <AccountButton onOpen={() => setIsAccountOpen(true)} />
              {!isAgent ? (
                <Link className="btn primary" to="/agent">
                  问问 Agent
                </Link>
              ) : null}
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
      <AccountModal open={isAccountOpen} onClose={() => setIsAccountOpen(false)} />
    </>
  );
}
