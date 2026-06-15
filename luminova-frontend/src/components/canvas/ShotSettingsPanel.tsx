import { RefreshCw } from 'lucide-react';

const cameraMoves = ['静止', '推进', '拉远', '环绕'] as const;
const shotSizes = ['大远景', '远景', '中景', '近景', '特写'] as const;
const lightings = ['夜景', '日景', '黄昏', '室内'] as const;

export function ShotSettingsPanel() {
  return (
    <aside className="canvas-inspector" aria-label="镜头设置">
      <div className="canvas-panel">
        <div className="canvas-panel__title">
          <span>镜头设置</span>
          <small>Shot 03</small>
        </div>

        <div className="shot-setting">
          <label>镜头运动</label>
          <div className="shot-setting__segmented">
            {cameraMoves.map((move, index) => (
              <button
                key={move}
                className={index === 1 ? 'is-active' : ''}
                type="button"
              >
                {move}
              </button>
            ))}
          </div>
          <div className="shot-setting__slider">
            <span>运动强度</span>
            <input type="range" min={0} max={100} defaultValue={45} />
            <b>0.45</b>
          </div>
        </div>

        <div className="shot-setting">
          <label>景别</label>
          <div className="shot-setting__icons">
            {shotSizes.map((size, index) => (
              <button
                key={size}
                className={index === 2 ? 'is-active' : ''}
                type="button"
                title={size}
              >
                <i />
                <span>{size}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="shot-setting">
          <label>时长</label>
          <div className="shot-setting__slider">
            <input type="range" min={2} max={12} step={0.5} defaultValue={6} />
            <b>6.0s</b>
          </div>
        </div>

        <div className="shot-setting">
          <label>光线</label>
          <div className="shot-setting__icons shot-setting__icons--light">
            {lightings.map((light, index) => (
              <button
                key={light}
                className={index === 0 ? 'is-active' : ''}
                type="button"
                title={light}
              >
                <i />
                <span>{light}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="shot-setting">
          <label>模型</label>
          <select className="shot-setting__select" defaultValue="cine-v2">
            <option value="cine-v2">Luminova Cine v2</option>
            <option value="cine-v1">Luminova Cine v1</option>
          </select>
        </div>

        <div className="shot-setting shot-setting--row">
          <div>
            <label>Seed</label>
            <div className="shot-setting__seed">
              <input type="text" defaultValue="428731" readOnly />
              <button type="button" aria-label="刷新 Seed">
                <RefreshCw size={14} />
              </button>
            </div>
          </div>
        </div>

        <div className="shot-setting">
          <label>参考强度</label>
          <div className="shot-setting__slider">
            <input type="range" min={0} max={100} defaultValue={68} />
            <b>0.68</b>
          </div>
        </div>

        <div className="canvas-panel__footer">
          <button className="canvas-btn canvas-btn--ghost" type="button">
            重置当前镜头
          </button>
          <button className="canvas-btn canvas-btn--primary" type="button">
            应用设置
          </button>
        </div>
      </div>
    </aside>
  );
}
