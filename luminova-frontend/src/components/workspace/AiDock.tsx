import { useState } from 'react';
import { Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { composerDefaults, composerOptions } from '../../data/workspace';
import { toProjectType } from '../../hooks/useWorkspaceProjects';
import type { ProjectType } from '../../lib/api/types';
import type { TableProject } from '../../data/workspace';

type AiDockProps = {
  isCreating: boolean;
  error: string | null;
  onCreateProject: (data: { name: string; projectType: ProjectType }) => Promise<TableProject>;
};

export function AiDock({ isCreating, error, onCreateProject }: AiDockProps) {
  const navigate = useNavigate();
  const [name, setName] = useState('赛博国风广告');
  const [prompt, setPrompt] = useState(composerDefaults.prompt);
  const [videoType, setVideoType] = useState(composerDefaults.videoType);
  const [aspectRatio, setAspectRatio] = useState(composerDefaults.aspectRatio);
  const [duration, setDuration] = useState(composerDefaults.duration);
  const [style, setStyle] = useState(composerDefaults.style);
  const [localError, setLocalError] = useState<string | null>(null);

  const handleSubmit = async () => {
    const projectName = name.trim();
    if (!projectName) {
      setLocalError('请先填写项目名称');
      return;
    }

    setLocalError(null);
    try {
      const project = await onCreateProject({
        name: projectName,
        projectType: toProjectType(videoType),
      });
      navigate(`/canvas?studio=${project.id}&enter=1`);
    } catch {
      // The workspace-level error banner shows the API error.
    }
  };

  return (
    <article className="composer-card glass-gradient" id="workspace-ai-dock" aria-label="用 AI 新建项目">
      <div className="composer-card__head">
        <Sparkles size={16} aria-hidden="true" />
        <h2>用 AI 新建项目</h2>
      </div>
      <label className="composer-field">
        <span>项目名称</span>
        <input
          className="composer-card__input"
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder="例如：新品宣传片 / 都市短剧 / 智能产品广告"
        />
      </label>
      <label className="composer-field">
        <span>创作灵感描述</span>
        <textarea
          value={prompt}
          onChange={(event) => setPrompt(event.target.value)}
          placeholder="描述你的创意、故事、画面或想表达的核心内容..."
        />
      </label>
      <div className="composer-card__controls">
        <label>
          <span>类型</span>
          <select value={videoType} onChange={(event) => setVideoType(event.target.value)}>
            {composerOptions.videoTypes.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
        </label>
        <label>
          <span>画幅</span>
          <select value={aspectRatio} onChange={(event) => setAspectRatio(event.target.value)}>
            {composerOptions.aspectRatios.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
        </label>
        <label>
          <span>时长</span>
          <select value={duration} onChange={(event) => setDuration(event.target.value)}>
            {composerOptions.durations.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
        </label>
        <label>
          <span>风格</span>
          <select value={style} onChange={(event) => setStyle(event.target.value)}>
            {composerOptions.styles.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
        </label>
      </div>
      {localError || error ? (
        <p className="composer-card__error">{localError ?? error}</p>
      ) : null}
      <button
        className="btn primary composer-card__submit"
        type="button"
        onClick={handleSubmit}
        disabled={isCreating}
      >
        <Sparkles size={15} aria-hidden="true" />
        {isCreating ? '创建中...' : '生成画布'}
      </button>
    </article>
  );
}
