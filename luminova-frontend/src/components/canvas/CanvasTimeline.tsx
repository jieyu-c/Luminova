import { Hand, Maximize2, Minus, Plus } from 'lucide-react';
import { canvasShots } from '../../data/canvas';

type CanvasTimelineProps = {
  zoom: number;
  onZoomChange: (zoom: number) => void;
  onFitView: () => void;
};

export function CanvasTimeline({ zoom, onZoomChange, onFitView }: CanvasTimelineProps) {
  return (
    <footer className="canvas-timeline">
      <div className="canvas-timeline__meta">
        <span>项目时长</span>
        <strong>00:26 / 01:30</strong>
      </div>

      <div className="canvas-timeline__shots">
        {canvasShots.map((shot) => (
          <button
            key={shot.id}
            className={`canvas-timeline__shot ${shot.active ? 'is-active' : ''}`}
            type="button"
          >
            <img src={shot.image} alt="" />
            <div>
              <b>{shot.label}</b>
              <span>{shot.title}</span>
              <i>{shot.duration}</i>
            </div>
          </button>
        ))}
      </div>

      <div className="canvas-timeline__controls">
        <button className="canvas-icon-btn is-active" type="button" title="平移" aria-label="平移">
          <Hand size={16} />
        </button>
        <button className="canvas-icon-btn" type="button" title="适配画布" aria-label="适配画布" onClick={onFitView}>
          <Maximize2 size={16} />
        </button>
        <div className="canvas-timeline__zoom">
          <button
            className="canvas-icon-btn"
            type="button"
            aria-label="缩小"
            onClick={() => onZoomChange(Math.max(0.4, zoom - 0.08))}
          >
            <Minus size={14} />
          </button>
          <input
            type="range"
            min={40}
            max={140}
            value={Math.round(zoom * 100)}
            onChange={(event) => onZoomChange(Number(event.target.value) / 100)}
          />
          <button
            className="canvas-icon-btn"
            type="button"
            aria-label="放大"
            onClick={() => onZoomChange(Math.min(1.4, zoom + 0.08))}
          >
            <Plus size={14} />
          </button>
          <span>{Math.round(zoom * 100)}%</span>
        </div>
      </div>
    </footer>
  );
}
