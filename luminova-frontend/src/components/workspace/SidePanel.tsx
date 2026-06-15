import type { CSSProperties } from 'react';
import { ArrowRight, Lightbulb } from 'lucide-react';
import { agentSuggestion, missingAssets, recommendedTemplates } from '../../data/workspace';

export function SidePanel() {
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
