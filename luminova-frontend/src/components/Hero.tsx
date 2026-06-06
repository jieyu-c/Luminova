import { Sparkles } from 'lucide-react';
import { CanvasPreview } from './canvas/CanvasPreview';

export function Hero() {
  return (
    <section className="landing-hero" id="workflow">
      <div className="hero-main">
        <div className="eyebrow">
          <Sparkles size={16} />
          Neural Video Canvas
        </div>
        <h1>
          Luminova<strong>·</strong>
          <em>灵衍</em>
          <span>AI 视频 · 无限画布工作流</span>
        </h1>
        <p className="lead">
          <strong>一句话生成可编辑的视频画布。</strong>
          <span>Agent 自动拆剧本、建分镜、继承角色与场景上下文。</span>
        </p>

        <div className="ai-composer glass-gradient">
          <div className="composer-head">
            <b>灵感输入</b>
            <span className="chip live">Agent Ready</span>
          </div>
          <div className="prompt-line">
            <span>雨夜便利店里的未来录像</span>
            <button className="btn ghost-cta" type="button">
              快速体验
            </button>
          </div>
          <div className="composer-flow" aria-label="自动生成链路">
            <span>拆剧本</span>
            <i />
            <span>建画布</span>
            <i />
            <span>出首版</span>
          </div>
        </div>

        <div className="hero-metrics">
          <Metric value="7+" label="节点类型" />
          <Metric value="10min" label="到首个片段" />
          <Metric value="4+" label="资产可复用" />
        </div>
      </div>

      <aside className="hero-signal hero-canvas" aria-label="画布预览">
        <CanvasPreview />
      </aside>
    </section>
  );
}

function Metric({ value, label }: { value: string; label: string }) {
  return (
    <div className="metric">
      <strong>{value}</strong>
      <span>{label}</span>
    </div>
  );
}
