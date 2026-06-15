import type { CSSProperties } from 'react';
import { agentSteps, productFlowNodes } from '../data/home';
import { cn } from '../lib/cn';

export function ProductShowcase() {
  return (
    <div className="product-flow-preview" aria-label="Luminova 画布工作流">
      <header className="product-flow-preview__header">
        <div className="product-flow-preview__title">
          <span className="chip tech">Generation Graph</span>
          <span>雨夜便利店 · Shot 03</span>
        </div>
        <div className="product-flow-preview__meta">
          <span className="chip">9:16</span>
          <span className="chip tech">cinematic-v3</span>
        </div>
      </header>

      <div className="product-flow-preview__stage">
        <div className="product-flow-preview__track" aria-label="创作节点链路">
          {productFlowNodes.map((node) => (
            <article
              key={node.id}
              className={cn('flow-node', node.featured && 'flow-node--featured')}
              style={{ '--node-accent': node.accent } as CSSProperties}
            >
              <span className="flow-node__type">{node.type}</span>
              {node.image ? (
                <img className="flow-node__thumb" src={node.image} alt="" />
              ) : null}
              {node.images ? (
                <div className="flow-node__grid">
                  {node.images.map((image, index) => (
                    <img key={`${node.id}-${index}`} src={image} alt="" />
                  ))}
                </div>
              ) : null}
              <strong>{node.title}</strong>
              <small>{node.detail}</small>
            </article>
          ))}
        </div>

        <footer className="product-flow-preview__agent">
          <span>Agent 正在整理创作链路</span>
          <ol>
            {agentSteps.map((step) => (
              <li
                key={step.label}
                className={
                  step.status === 'done'
                    ? 'done'
                    : step.status === 'active'
                      ? 'active'
                      : undefined
                }
              >
                {step.label}
              </li>
            ))}
          </ol>
        </footer>
      </div>
    </div>
  );
}
