import type { CSSProperties } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { currentProject } from '../../data/workspace';
import { useStudioTransition } from '../../lib/useStudioTransition';

export function ContinueProjectCard() {
  const { enterStudio, captureTransition } = useStudioTransition();
  const canvasUrl = `/canvas?studio=${currentProject.id}&enter=1`;

  return (
    <article className="continue-card">
      <Link
        to={canvasUrl}
        className="continue-card__link"
        aria-label={`继续 ${currentProject.title}`}
        onPointerDown={(event) => captureTransition(event, currentProject)}
        onClick={(event) => enterStudio(event, currentProject)}
      >
        <div className="continue-cover" aria-hidden="true">
          <span>{currentProject.title}</span>
        </div>
        <div className="continue-card__body">
          <div className="continue-card__head">
            <h2>继续{currentProject.title}</h2>
            <span className="status-badge status-badge--active">{currentProject.status}</span>
          </div>
          <div
            className="progress progress--labeled"
            style={{ '--accent': currentProject.accent } as CSSProperties}
          >
            <div className="progress__meta">
              <span>进度</span>
              <strong>{currentProject.progress}%</strong>
            </div>
            <i style={{ '--value': `${currentProject.progress}%` } as CSSProperties} />
          </div>
          <p className="continue-card__next">
            下一步：<strong>{currentProject.nextStep}</strong>
          </p>
          <span className="btn btn-outline continue-card__cta">
            进入画布
            <ArrowRight size={15} aria-hidden="true" />
          </span>
        </div>
      </Link>
    </article>
  );
}
