import type { CSSProperties } from 'react';
import { ArrowRight, CheckCircle2, Clapperboard, FileVideo, Lightbulb, MessageSquareText, PanelsTopLeft } from 'lucide-react';
import { agentSuggestion, missingAssets, recommendedTemplates } from '../../data/workspace';

const starterTemplates = [
  { id: 'ad-30', title: '30 秒广告', detail: '产品卖点 · 品牌展示 · 快节奏', icon: FileVideo },
  { id: 'drama-ep1', title: '短剧第一集', detail: '剧情驱动 · 角色塑造 · 钩子完整', icon: Clapperboard },
  { id: 'comic-shot', title: '漫剧分镜', detail: '分镜预演 · 节奏把控 · 视觉风格', icon: PanelsTopLeft },
  { id: 'talking-head', title: '口播脚本', detail: '真人出镜 · 信息传达 · 专业表达', icon: MessageSquareText },
];

const creationPrep = ['写一句创意', '选择视频类型', '确认画幅和时长'];

export function SidePanel({ hasProjects = true }: { hasProjects?: boolean }) {
  if (!hasProjects) {
    return (
      <aside className="side-panel" aria-label="首次创作辅助信息">
        <section className="side-block">
          <div className="side-block__head side-block__head--split">
            <h2>推荐起步模板</h2>
            <button className="section-link" type="button">
              更多模板
              <ArrowRight size={14} aria-hidden="true" />
            </button>
          </div>
          <ul className="starter-template-list">
            {starterTemplates.map((template) => {
              const Icon = template.icon;

              return (
                <li key={template.id}>
                  <span className="starter-template-list__icon" aria-hidden="true">
                    <Icon size={17} />
                  </span>
                  <div>
                    <b>{template.title}</b>
                    <p>{template.detail}</p>
                  </div>
                  <ArrowRight size={15} aria-hidden="true" />
                </li>
              );
            })}
          </ul>
        </section>

        <section className="side-block">
          <div className="side-block__head">
            <h2>创作准备</h2>
          </div>
          <ul className="creation-prep-list">
            {creationPrep.map((item) => (
              <li key={item}>
                <CheckCircle2 size={16} aria-hidden="true" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </section>
      </aside>
    );
  }

  return (
    <aside className="side-panel" aria-label="工作台辅助信息">
      <section className="side-block">
        <div className="side-block__head">
          <h2>Agent 建议</h2>
        </div>
        <article className="agent-card">
          <div className="agent-card__icon" aria-hidden="true">
            <Lightbulb size={18} />
          </div>
          <p>{agentSuggestion}</p>
          <button className="section-link" type="button">
            查看建议详情
            <ArrowRight size={14} aria-hidden="true" />
          </button>
        </article>
      </section>

      <section className="side-block">
        <div className="side-block__head">
          <h2>缺失素材</h2>
        </div>
        <ul className="asset-list">
          {missingAssets.map((asset, index) => (
            <li key={asset.id} className="asset-list__item">
              <span
                className="ws-thumb ws-thumb--sm"
                style={
                  {
                    '--accent':
                      index === 0
                        ? 'linear-gradient(135deg, var(--cyan), var(--node-scene))'
                        : 'linear-gradient(135deg, var(--blue), var(--violet))',
                  } as CSSProperties
                }
                aria-hidden="true"
              />
              <span className="asset-list__name">{asset.name}</span>
              <button className="btn btn-quiet btn-sm" type="button">
                去补拍
              </button>
            </li>
          ))}
        </ul>
      </section>

      <section className="side-block">
        <div className="side-block__head">
          <h2>推荐模板</h2>
        </div>
        <ul className="template-list">
          {recommendedTemplates.map((template) => (
            <li key={template.id} className="template-list__item">
              <span
                className="ws-thumb ws-thumb--sm"
                style={{ '--accent': template.accent } as CSSProperties}
                aria-hidden="true"
              />
              <div className="template-list__body">
                <b>{template.title}</b>
                <div className="template-list__meta">
                  {template.tags.map((tag) => (
                    <span key={tag} className="pill">
                      {tag}
                    </span>
                  ))}
                  <span className="template-list__usage">{template.usageCount} 次使用</span>
                </div>
              </div>
            </li>
          ))}
        </ul>
      </section>
    </aside>
  );
}
