import {
  ChevronDown,
  ChevronUp,
  Filter,
  FolderPlus,
  Plus,
  Search,
} from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  canvasLibraryItems,
  canvasProjects,
  type CanvasLibraryStatus,
} from '../../data/canvas-library';
import { cn } from '../../lib/cn';

const episodes = ['全部', '第 01 集', '第 02 集', '第 03 集', '第 04 集'];

const statusClass: Record<CanvasLibraryStatus, string> = {
  进行中: 'is-running',
  待确认: 'is-confirming',
  待审核: 'is-review',
  草稿: 'is-draft',
};

export function CanvasLibrary() {
  const [selectedProject, setSelectedProject] = useState('spirit-comic');
  const [selectedEpisode, setSelectedEpisode] = useState('全部');
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'全部' | CanvasLibraryStatus>('全部');
  const [isScrolledCompact, setIsScrolledCompact] = useState(false);
  const [isManuallyCollapsed, setIsManuallyCollapsed] = useState(false);

  const project = canvasProjects.find((item) => item.id === selectedProject);
  const isComicProject = project?.type === '漫剧';
  const isCompact = isManuallyCollapsed || isScrolledCompact;

  useEffect(() => {
    const handleScroll = () => setIsScrolledCompact(window.scrollY > 210);
    handleScroll();
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const filteredCanvases = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    return canvasLibraryItems.filter((canvas) => {
      const matchesProject =
        selectedProject === 'all' || canvas.projectId === selectedProject;
      const matchesEpisode =
        !isComicProject || selectedEpisode === '全部' || canvas.episode === selectedEpisode;
      const matchesStatus = statusFilter === '全部' || canvas.status === statusFilter;
      const matchesQuery =
        !normalizedQuery || canvas.title.toLowerCase().includes(normalizedQuery);

      return matchesProject && matchesEpisode && matchesStatus && matchesQuery;
    });
  }, [isComicProject, query, selectedEpisode, selectedProject, statusFilter]);

  const chooseProject = (projectId: string) => {
    setSelectedProject(projectId);
    setSelectedEpisode('全部');
  };

  const expandNavigation = () => {
    setIsManuallyCollapsed(false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <section className="canvas-library" aria-labelledby="canvas-library-title">
      <header className="canvas-library__header">
        <div className="canvas-library__title">
          <h1 id="canvas-library-title">画布库</h1>
          <span>共 {canvasLibraryItems.length} 个画布</span>
        </div>
        <label className="canvas-library__search">
          <Search size={18} aria-hidden="true" />
          <span className="sr-only">搜索画布</span>
          <input
            type="search"
            value={query}
            placeholder="搜索画布名称"
            onChange={(event) => setQuery(event.target.value)}
          />
        </label>
        <label className="canvas-library__filter">
          <Filter size={17} aria-hidden="true" />
          <span className="sr-only">筛选状态</span>
          <select
            value={statusFilter}
            onChange={(event) =>
              setStatusFilter(event.target.value as '全部' | CanvasLibraryStatus)
            }
          >
            <option value="全部">全部状态</option>
            <option value="进行中">进行中</option>
            <option value="待确认">待确认</option>
            <option value="待审核">待审核</option>
            <option value="草稿">草稿</option>
          </select>
        </label>
        <Link className="btn canvas-library__secondary-action" to="/canvas?new=project">
          <FolderPlus size={17} aria-hidden="true" />
          新建项目
        </Link>
        <Link className="btn primary canvas-library__primary-action" to="/canvas?new=canvas">
          <Plus size={18} aria-hidden="true" />
          新建画布
        </Link>
      </header>

      <div className={cn('canvas-project-nav-wrap', isCompact && 'is-compact')}>
        {isCompact ? (
          <div className="canvas-project-nav canvas-project-nav--compact">
            <div className="canvas-project-current">
              {project ? <img src={project.cover} alt="" /> : null}
              <div>
                <strong>{project?.name ?? '全部画布'}</strong>
                <span>
                  {project?.canvasCount ?? canvasLibraryItems.length} 个画布
                  {project ? ` · ${project.type}` : ''}
                </span>
              </div>
            </div>
            <label className="canvas-project-switcher">
              <span className="sr-only">切换项目</span>
              <select
                value={selectedProject}
                onChange={(event) => chooseProject(event.target.value)}
              >
                <option value="all">全部画布</option>
                {canvasProjects.map((item) => (
                  <option value={item.id} key={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
              <ChevronDown size={16} aria-hidden="true" />
            </label>
            {isComicProject ? (
              <ComicFilters
                compact
                selectedEpisode={selectedEpisode}
                onEpisodeChange={setSelectedEpisode}
              />
            ) : null}
            <button
              className="canvas-project-nav__toggle"
              type="button"
              onClick={expandNavigation}
            >
              <ChevronDown size={17} aria-hidden="true" />
              展开项目导航
            </button>
          </div>
        ) : (
          <>
            <div className="canvas-project-nav">
              <button
                className={cn('canvas-project-card', selectedProject === 'all' && 'active')}
                type="button"
                onClick={() => chooseProject('all')}
              >
                <span className="canvas-project-card__all" aria-hidden="true">
                  <span />
                  <span />
                  <span />
                  <span />
                </span>
                <span>
                  <strong>全部画布</strong>
                  <small>{canvasLibraryItems.length}</small>
                </span>
              </button>
              {canvasProjects.map((item) => (
                <button
                  className={cn(
                    'canvas-project-card',
                    selectedProject === item.id && 'active',
                  )}
                  type="button"
                  key={item.id}
                  onClick={() => chooseProject(item.id)}
                >
                  <img src={item.cover} alt="" />
                  <span>
                    <strong>{item.name}</strong>
                    <small>{item.canvasCount}</small>
                  </span>
                </button>
              ))}
              <button
                className="canvas-project-nav__toggle"
                type="button"
                onClick={() => setIsManuallyCollapsed(true)}
              >
                <ChevronUp size={17} aria-hidden="true" />
                收起项目导航
              </button>
            </div>
            {isComicProject ? (
              <ComicFilters
                selectedEpisode={selectedEpisode}
                onEpisodeChange={setSelectedEpisode}
              />
            ) : null}
          </>
        )}
      </div>

      {filteredCanvases.length ? (
        <div className="canvas-library-grid">
          {filteredCanvases.map((canvas) => (
            <Link
              className="canvas-library-card"
              to={`/canvas?studio=${canvas.projectId}&canvas=${canvas.id}&enter=1`}
              key={canvas.id}
              aria-label={`打开画布：${canvas.title}`}
            >
              <div className="canvas-library-card__image">
                <img src={canvas.image} alt="" />
              </div>
              <div className="canvas-library-card__body">
                <h2>{canvas.title}</h2>
                <div className="canvas-library-card__meta">
                  <span className={cn('canvas-status', statusClass[canvas.status])}>
                    <i aria-hidden="true" />
                    {canvas.status}
                  </span>
                  <time>{canvas.updatedAt}更新</time>
                </div>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <div className="canvas-library-empty">
          <Search size={28} aria-hidden="true" />
          <h2>没有找到匹配的画布</h2>
          <p>试试其他项目、集数或关键词。</p>
        </div>
      )}
    </section>
  );
}

function ComicFilters({
  compact = false,
  selectedEpisode,
  onEpisodeChange,
}: {
  compact?: boolean;
  selectedEpisode: string;
  onEpisodeChange: (episode: string) => void;
}) {
  return (
    <div className={cn('canvas-context-filter', compact && 'is-compact')}>
      {!compact ? (
        <span className="canvas-context-filter__type">
          项目类型：<strong>漫剧</strong>
        </span>
      ) : null}
      <label className="canvas-season-select">
        <span className="sr-only">选择季度</span>
        <select defaultValue="第一季">
          <option>第一季</option>
          <option>第二季</option>
        </select>
        <ChevronDown size={16} aria-hidden="true" />
      </label>
      <div className="canvas-episode-tabs" aria-label="选择集数">
        {episodes.map((episode) => (
          <button
            className={cn(selectedEpisode === episode && 'active')}
            type="button"
            key={episode}
            onClick={() => onEpisodeChange(episode)}
          >
            {episode}
          </button>
        ))}
      </div>
    </div>
  );
}

