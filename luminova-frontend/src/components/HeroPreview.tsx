import { agentChecklist, showcaseAssets } from '../data/home';

export function HeroPreview() {
  return (
    <div className="hero-preview" aria-label="产品预览摘要">
      <div className="agent-panel-head">
        <span>Agent Co-pilot</span>
        <b>自动把灵感拆成可执行工作流</b>
      </div>
      <div className="agent-checklist" aria-label="Agent 能力">
        {agentChecklist.map((item) => (
          <span
            key={item.label}
            className={
              item.status === 'done' ? 'done' : item.status === 'active' ? 'active' : undefined
            }
          >
            {item.label}
          </span>
        ))}
      </div>
      <div className="preview-card">
        <img src={showcaseAssets.projectRain} alt="雨夜悬疑短剧预览" />
        <div>
          <b>第 03 场 · 天台对峙</b>
          <small>角色一致性 92% · Prompt 已复用 12 次</small>
        </div>
      </div>
      <div className="agent-output">
        <span>下一步建议</span>
        <strong>补充反打镜头，并生成 3 个低饱和光影版本。</strong>
      </div>
    </div>
  );
}
