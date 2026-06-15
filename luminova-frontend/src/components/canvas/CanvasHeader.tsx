import { ChevronDown, LayoutGrid, Play, Redo2, Undo2, Upload } from 'lucide-react';
import { Link } from 'react-router-dom';
import { brandAssets } from '../../data/brand';

const canvasTabs = ['创作画布', '故事板', '影片预览'] as const;

export function CanvasHeader() {
  return (
    <header className="canvas-header">
      <div className="canvas-header__left">
        <Link className="canvas-brand" to="/" aria-label="Luminova 灵衍首页">
          <img
            className="canvas-brand__mark"
            src={brandAssets.logoMarkDark}
            alt=""
            width={34}
            height={34}
          />
          <span className="canvas-brand__word">
            <b>Luminova</b>
            <em>灵衍</em>
          </span>
        </Link>
        <button className="canvas-project" type="button">
          雨夜追踪 · 短片项目
          <ChevronDown size={16} />
        </button>
      </div>

      <nav className="canvas-tabs" aria-label="画布视图">
        {canvasTabs.map((tab, index) => (
          <button
            key={tab}
            className={`canvas-tabs__item ${index === 0 ? 'is-active' : ''}`}
            type="button"
          >
            {tab}
          </button>
        ))}
      </nav>

      <div className="canvas-header__actions">
        <button className="canvas-icon-btn" type="button" title="撤销" aria-label="撤销">
          <Undo2 size={17} />
        </button>
        <button className="canvas-icon-btn" type="button" title="重做" aria-label="重做">
          <Redo2 size={17} />
        </button>
        <button className="canvas-btn canvas-btn--ghost" type="button">
          <Play size={16} />
          播放
        </button>
        <button className="canvas-btn canvas-btn--ghost" type="button">
          <Upload size={16} />
          导出
        </button>
        <button className="canvas-icon-btn" type="button" title="布局" aria-label="布局">
          <LayoutGrid size={17} />
        </button>
      </div>
    </header>
  );
}
