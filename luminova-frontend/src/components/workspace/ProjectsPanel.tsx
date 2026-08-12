import type { CSSProperties } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, FolderPlus, RefreshCw } from 'lucide-react';
import type { ProjectStatus, TableProject } from '../../data/workspace';
import { useStudioTransition } from '../../lib/useStudioTransition';
import { cn } from '../../lib/cn';

const statusClass: Record<ProjectStatus, string> = {
  进行中: 'project-status--active',
  待审核: 'project-status--review',
  已完成: 'project-status--done',
  草稿: 'project-status--draft',
};

type ProjectsPanelProps = {
  projects: TableProject[];
  isLoading: boolean;
  error: string | null;
  onRetry: () => void;
};

export function ProjectsPanel({ projects, isLoading, error, onRetry }: ProjectsPanelProps) {
  const { enterStudio, captureTransition } = useStudioTransition();

  return (
    <section className="recent-projects" aria-label="最近项目">
      <div className="section-head">
        <h2>最近项目</h2>
        <button className="section-link" type="button" onClick={onRetry} disabled={isLoading}>
          <RefreshCw size={14} aria-hidden="true" />
          刷新
        </button>
      </div>

      {error ? (
        <div className="workspace-message workspace-message--error">
          <span>{error}</span>
          <button type="button" onClick={onRetry}>重试</button>
        </div>
      ) : null}

      <div className="project-table-wrap">
        <table className="project-table">
          <caption className="sr-only">最近项目列表</caption>
          <thead>
            <tr>
              <th scope="col">项目名称</th>
              <th scope="col">类型</th>
              <th scope="col">画幅</th>
              <th scope="col">时长</th>
              <th scope="col">进度</th>
              <th scope="col">状态</th>
              <th scope="col">更新时间</th>
              <th scope="col">操作</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={8} className="project-table__empty">
                  正在加载项目...
                </td>
              </tr>
            ) : null}

            {!isLoading && projects.length === 0 ? (
              <tr>
                <td colSpan={8} className="project-table__empty">
                  <div className="project-empty-state">
                    <FolderPlus size={30} aria-hidden="true" />
                    <b>暂无项目</b>
                    <p>创建后，你的广告、短剧、漫剧和口播项目会出现在这里。</p>
                    <a className="btn btn-outline btn-sm" href="#workspace-ai-dock">
                      创建项目
                    </a>
                  </div>
                </td>
              </tr>
            ) : null}

            {!isLoading && projects.map((project) => {
              const canvasUrl = `/canvas?studio=${project.id}&enter=1`;

              return (
              <tr key={project.id}>
                <td className="project-table__name">
                  <Link
                    className="project-table__identity"
                    to={canvasUrl}
                    aria-label={`进入 ${project.title} 画布`}
                    onPointerDown={(event) => captureTransition(event, project)}
                    onClick={(event) => enterStudio(event, project)}
                  >
                    <span
                      className="ws-thumb"
                      style={{ '--accent': project.accent } as CSSProperties}
                      aria-hidden="true"
                    />
                    <span>{project.title}</span>
                  </Link>
                </td>
                <td>{project.type}</td>
                <td>{project.aspectRatio}</td>
                <td>{project.duration}</td>
                <td className="project-table__progress">
                  <div
                    className="progress progress--compact"
                    style={{ '--accent': project.accent } as CSSProperties}
                  >
                    <i style={{ '--value': `${project.progress}%` } as CSSProperties} />
                  </div>
                  <span>{project.progress}%</span>
                </td>
                <td>
                  <span className={cn('project-status', statusClass[project.status])}>
                    {project.status}
                  </span>
                </td>
                <td className="project-table__time">{project.updatedAt}</td>
                <td>
                  <Link
                    className="table-action"
                    to={canvasUrl}
                    aria-label={`进入 ${project.title} 画布`}
                    onPointerDown={(event) => captureTransition(event, project)}
                    onClick={(event) => enterStudio(event, project)}
                  >
                    进入画布
                    <ArrowRight size={14} aria-hidden="true" />
                  </Link>
                </td>
              </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}
