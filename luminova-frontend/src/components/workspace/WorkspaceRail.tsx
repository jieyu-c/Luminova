import type { LucideIcon } from 'lucide-react';
import { Box, Circle, Hexagon, LayoutGrid } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { cn } from '../../lib/cn';

const RAIL_MIN_WIDTH = 72;
const RAIL_MAX_WIDTH = 240;
const RAIL_EXPANDED_THRESHOLD = 132;
const RAIL_STORAGE_KEY = 'luminova.workspaceRailWidth';

export type WorkspaceView = 'projects' | 'canvases';

type RailItem =
  | { kind: 'view'; view: WorkspaceView; label: string; icon: LucideIcon }
  | { kind: 'disabled'; label: string; icon: LucideIcon };

const railItems: RailItem[] = [
  { kind: 'view', view: 'projects', label: '项目', icon: LayoutGrid },
  { kind: 'view', view: 'canvases', label: '画布', icon: Circle },
  { kind: 'disabled', label: '资产', icon: Box },
  { kind: 'disabled', label: '模板', icon: Hexagon },
];

export function WorkspaceRail({
  activeView,
  onViewChange,
}: {
  activeView: WorkspaceView;
  onViewChange: (view: WorkspaceView) => void;
}) {
  const [railWidth, setRailWidth] = useState(RAIL_MIN_WIDTH);
  const dragStartRef = useRef({ pointerX: 0, width: RAIL_MIN_WIDTH });
  const isExpanded = railWidth >= RAIL_EXPANDED_THRESHOLD;

  useEffect(() => {
    const savedWidth = Number(window.localStorage.getItem(RAIL_STORAGE_KEY));

    if (Number.isFinite(savedWidth)) {
      setRailWidth(Math.min(RAIL_MAX_WIDTH, Math.max(RAIL_MIN_WIDTH, savedWidth)));
    }
  }, []);

  const updateRailWidth = (nextWidth: number) => {
    const clampedWidth = Math.min(RAIL_MAX_WIDTH, Math.max(RAIL_MIN_WIDTH, nextWidth));
    setRailWidth(clampedWidth);
    window.localStorage.setItem(RAIL_STORAGE_KEY, String(clampedWidth));
  };

  const handleResizeStart = (event: React.PointerEvent<HTMLDivElement>) => {
    dragStartRef.current = { pointerX: event.clientX, width: railWidth };
    event.currentTarget.setPointerCapture(event.pointerId);
    document.body.classList.add('is-resizing-rail');
  };

  const handleResizeMove = (event: React.PointerEvent<HTMLDivElement>) => {
    if (!event.currentTarget.hasPointerCapture(event.pointerId)) {
      return;
    }

    const delta = event.clientX - dragStartRef.current.pointerX;
    updateRailWidth(dragStartRef.current.width + delta);
  };

  const handleResizeEnd = (event: React.PointerEvent<HTMLDivElement>) => {
    if (event.currentTarget.hasPointerCapture(event.pointerId)) {
      event.currentTarget.releasePointerCapture(event.pointerId);
    }

    document.body.classList.remove('is-resizing-rail');
  };

  return (
    <nav
      className={cn('rail', isExpanded && 'rail--expanded')}
      aria-label="工作区导航"
      style={{ width: railWidth }}
    >
      {railItems.map((item) => {
        const Icon = item.icon;

        if (item.kind === 'disabled') {
          return (
            <span
              key={item.label}
              className="rail-link rail-link--disabled"
              title={isExpanded ? undefined : `${item.label}（即将上线）`}
              aria-label={`${item.label}（即将上线）`}
            >
              <Icon size={18} aria-hidden="true" />
              <span className="rail-label">{item.label}</span>
            </span>
          );
        }

        const isActive = activeView === item.view;

        return (
          <button
            key={item.label}
            type="button"
            className={cn('rail-view-button', isActive && 'active')}
            title={isExpanded ? undefined : item.label}
            aria-label={item.label}
            aria-current={isActive ? 'page' : undefined}
            onClick={() => onViewChange(item.view)}
          >
            <Icon size={18} aria-hidden="true" />
            <span className="rail-label">{item.label}</span>
          </button>
        );
      })}
      <div
        className="rail-resizer"
        role="separator"
        aria-label="调整工作区导航宽度"
        aria-orientation="vertical"
        aria-valuemin={RAIL_MIN_WIDTH}
        aria-valuemax={RAIL_MAX_WIDTH}
        aria-valuenow={Math.round(railWidth)}
        tabIndex={0}
        onDoubleClick={() =>
          updateRailWidth(isExpanded ? RAIL_MIN_WIDTH : RAIL_MAX_WIDTH)
        }
        onKeyDown={(event) => {
          if (event.key === 'ArrowLeft') {
            event.preventDefault();
            updateRailWidth(railWidth - 16);
          } else if (event.key === 'ArrowRight') {
            event.preventDefault();
            updateRailWidth(railWidth + 16);
          } else if (event.key === 'Home') {
            event.preventDefault();
            updateRailWidth(RAIL_MIN_WIDTH);
          } else if (event.key === 'End') {
            event.preventDefault();
            updateRailWidth(RAIL_MAX_WIDTH);
          }
        }}
        onPointerDown={handleResizeStart}
        onPointerMove={handleResizeMove}
        onPointerUp={handleResizeEnd}
        onPointerCancel={handleResizeEnd}
      />
    </nav>
  );
}
