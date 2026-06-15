import { useState } from 'react';
import { Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import { composerDefaults, composerOptions } from '../../data/workspace';

export function AiDock() {
  const [prompt, setPrompt] = useState(composerDefaults.prompt);
  const [videoType, setVideoType] = useState(composerDefaults.videoType);
  const [aspectRatio, setAspectRatio] = useState(composerDefaults.aspectRatio);
  const [duration, setDuration] = useState(composerDefaults.duration);
  const [style, setStyle] = useState(composerDefaults.style);

  return (
    <article className="composer-card glass-gradient" aria-label="用 AI 新建项目">
      <div className="composer-card__head">
        <Sparkles size={16} aria-hidden="true" />
        <h2>用 AI 新建项目</h2>
      </div>
      <textarea
        value={prompt}
        onChange={(event) => setPrompt(event.target.value)}
        aria-label="创作灵感描述"
      />
      <div className="composer-card__controls">
        <select
          value={videoType}
          onChange={(event) => setVideoType(event.target.value)}
          aria-label="视频类型"
        >
          {composerOptions.videoTypes.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
        <select
          value={aspectRatio}
          onChange={(event) => setAspectRatio(event.target.value)}
          aria-label="画幅比例"
        >
          {composerOptions.aspectRatios.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
        <select
          value={duration}
          onChange={(event) => setDuration(event.target.value)}
          aria-label="目标时长"
        >
          {composerOptions.durations.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
        <select
          value={style}
          onChange={(event) => setStyle(event.target.value)}
          aria-label="默认风格"
        >
          {composerOptions.styles.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </div>
      <Link className="btn primary composer-card__submit" to="/canvas">
        <Sparkles size={15} aria-hidden="true" />
        生成画布
      </Link>
    </article>
  );
}
