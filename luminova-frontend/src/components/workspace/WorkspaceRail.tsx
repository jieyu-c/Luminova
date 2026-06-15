import type { LucideIcon } from 'lucide-react';
import { Box, Circle, Hexagon, LayoutGrid } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { LuminovaMark } from '../brand/LuminovaMark';
import { cn } from '../../lib/cn';

type RailItem =
  | { kind: 'internal'; to: string; label: string; icon: LucideIcon }
  | { kind: 'disabled'; label: string; icon: LucideIcon };

const railItems: RailItem[] = [
  { kind: 'internal', to: '/workspace', label: '项目', icon: LayoutGrid },
  { kind: 'internal', to: '/canvas', label: '画布', icon: Circle },
  { kind: 'disabled', label: '资产', icon: Box },
  { kind: 'disabled', label: '模板', icon: Hexagon },
];

export function WorkspaceRail() {
  const { pathname } = useLocation();

  return (
    <nav className="rail" aria-label="工作区导航">
      <LuminovaMark className="rail-logo" size={40} />
      {railItems.map((item) => {
        const Icon = item.icon;

        if (item.kind === 'disabled') {
          return (
            <span
              key={item.label}
              className="rail-link rail-link--disabled"
              title={`${item.label}（即将上线）`}
              aria-label={`${item.label}（即将上线）`}
            >
              <Icon size={18} aria-hidden="true" />
            </span>
          );
        }

        const isActive = pathname === item.to;

        return (
          <Link
            key={item.label}
            to={item.to}
            className={cn(isActive && 'active')}
            title={item.label}
            aria-label={item.label}
            aria-current={isActive ? 'page' : undefined}
          >
            <Icon size={18} aria-hidden="true" />
          </Link>
        );
      })}
    </nav>
  );
}
