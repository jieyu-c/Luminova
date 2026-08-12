import type { CSSProperties } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Clapperboard, FileText, Image, Lightbulb, PlaySquare, Sparkles, UserRound } from 'lucide-react';
import type { TableProject } from '../../data/workspace';
import { useStudioTransition } from '../../lib/useStudioTransition';

const emptyCanvasNodes = [
  { id: 'idea', label: '创意输入', icon: Lightbulb, className: 'idea' },
  { id: 'script', label: '剧本解析', icon: FileText, className: 'script' },
  { id: 'role', label: '角色设定', icon: UserRound, className: 'role' },
  { id: 'scene', label: '场景库', icon: Image, className: 'scene' },
  { id: 'shot', label: '分镜', icon: Clapperboard, className: 'shot' },
  { id: 'prompt', label: 'Prompt', icon: Sparkles, className: 'prompt' },
  { id: 'video', label: '视频结果', icon: PlaySquare, className: 'video' },
];

type ContinueProjectCardProps = {
  project: TableProject | null;
  isLoading: boolean;
};

export function ContinueProjectCard({ project, isLoading }: ContinueProjectCardProps) {
  const { enterStudio, captureTransition } = useStudioTransition();

  if (isLoading) {
    return (
      <article className="continue-card continue-card--empty" aria-busy="true">
        <div className="continue-cover" aria-hidden="true">
          <span>加载中</span>
        </div>
        <div className="continue-card__body">
          <div className="continue-card__head">
            <h2>正在读取项目</h2>
          </div>
          <p className="continue-card__next">同步你的最近创作记录。</p>
        </div>
      </article>
    );
  }

  if (!project) {
    return (
      <article className="continue-card continue-card--empty workspace-empty-hero">
        <div className="workspace-empty-hero__copy">
          <span className="eyebrow">First Canvas</span>
          <h2>还没有项目</h2>
          <p>
            输入一个创意，Luminova 会为你创建第一张可编辑的创作 Canvas。
          </p>
          <div className="workspace-empty-hero__actions">
            <a className="btn primary" href="#workspace-ai-dock">
              <Sparkles size={15} aria-hidden="true" />
              生成画布
            </a>
            <a className="btn btn-outline" href="/canvas?demo=1">
              <PlaySquare size={15} aria-hidden="true" />
              查看示例 Canvas
            </a>
          </div>
        </div>
        <div className="empty-canvas-preview" aria-label="创作 Canvas 节点预览">
          <div className="empty-canvas-preview__grid" aria-hidden="true" />
          <svg className="empty-canvas-preview__edges" viewBox="0 0 560 272" aria-hidden="true">
            <path d="M128 70 H230" />
            <path d="M330 70 H430" />
            <path d="M280 96 V138" />
            <path d="M128 164 H230" />
            <path d="M330 164 H430" />
            <path d="M280 190 V218" />
            <path d="M478 92 C522 100 524 156 476 164" />
            <path d="M150 190 C188 224 238 226 250 226" />
          </svg>
          <div className="empty-canvas-preview__nodes">
            {emptyCanvasNodes.map((node) => {
              const Icon = node.icon;

              return (
                <div
                  key={node.id}
                  className={`empty-canvas-node empty-canvas-node--${node.className}`}
                >
                  <Icon size={17} aria-hidden="true" />
                  <span>{node.label}</span>
                </div>
              );
            })}
          </div>
        </div>
      </article>
    );
  }

  const canvasUrl = `/canvas?studio=${project.id}&enter=1`;

  return (
    <article className="continue-card">
      <Link
        to={canvasUrl}
        className="continue-card__link"
        aria-label={`继续 ${project.title}`}
        onPointerDown={(event) => captureTransition(event, project)}
        onClick={(event) => enterStudio(event, project)}
      >
        <div className="continue-cover" aria-hidden="true">
          <span>{project.title}</span>
        </div>
        <div className="continue-card__body">
          <div className="continue-card__head">
            <h2>继续{project.title}</h2>
            <span className="status-badge status-badge--active">{project.status}</span>
          </div>
          <div
            className="progress progress--labeled"
            style={{ '--accent': project.accent } as CSSProperties}
          >
            <div className="progress__meta">
              <span>进度</span>
              <strong>{project.progress}%</strong>
            </div>
            <i style={{ '--value': `${project.progress}%` } as CSSProperties} />
          </div>
          <p className="continue-card__next">
            下一步：<strong>进入画布继续创作</strong>
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
