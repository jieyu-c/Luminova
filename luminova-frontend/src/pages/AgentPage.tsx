import {
  ArrowRight,
  ImagePlus,
  Mic,
  Paperclip,
  Send,
  Sparkles,
} from 'lucide-react';
import { FormEvent, useMemo, useState } from 'react';
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { Header } from '../components/Header';
import { useAuth } from '../contexts/AuthContext';

type AgentProject = {
  id: string;
  title: string;
  image: string;
  detail: string;
  meta: string;
  progress?: number;
};

const projects: AgentProject[] = [
  {
    id: 'rainy-store',
    title: '雨夜便利店',
    image: '/prototype/home-showcase-assets/project-rain.png',
    detail: '下一步：确认镜头 04 的 Prompt',
    meta: '分镜 04 · 今天 10:15',
    progress: 68,
  },
  {
    id: 'tea-ad',
    title: '茶叶广告 Demo',
    image: '/prototype/home-showcase-assets/project-ad.png',
    detail: '脚本已经收好，就差第一版分镜',
    meta: '广告 · 16:9 · 昨天 18:47',
  },
  {
    id: 'campus-ep3',
    title: '校园漫剧第三集',
    image: '/prototype/home-showcase-assets/project-anime.png',
    detail: '角色一致性检查中',
    meta: '漫剧 · 1:1 · 3 天前',
  },
  {
    id: 'voice-ab',
    title: '口播剧情 A/B',
    image: '/prototype/home-showcase-assets/video-output.png',
    detail: '两个版本都在等你拍板',
    meta: '口播 · 9:16 · 5 天前',
  },
];

const returningPrompts = ['继续雨夜便利店', '打开茶叶广告 Demo', '来支 15 秒的小广告'];
const newUserPrompts = ['制作一支产品广告', '把脚本变成短剧', '先开张白纸'];

function parseProjectCount(value: string | null) {
  if (value === null) return projects.length;
  const parsed = Number.parseInt(value, 10);
  return Number.isNaN(parsed) ? projects.length : Math.max(0, Math.min(parsed, projects.length));
}

export function AgentPage() {
  const { isAuthenticated, isLoading, user } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const projectCount = parseProjectCount(searchParams.get('projects'));
  const visibleProjects = useMemo(() => projects.slice(0, projectCount), [projectCount]);
  const recentProject = visibleProjects[0];
  const otherProjects = visibleProjects.slice(1, 4);
  const [prompt, setPrompt] = useState('');

  if (isLoading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  const displayName = user?.nickname || user?.username || '创作者';
  const isNewUser = visibleProjects.length === 0;
  const promptSuggestions = isNewUser ? newUserPrompts : returningPrompts;

  const openCanvas = (projectId?: string) => {
    navigate(projectId ? `/canvas?studio=${projectId}` : '/canvas');
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const normalized = prompt.trim();
    if (!normalized) return;

    const matchedProject = projects.find((project) => normalized.includes(project.title.replace(' Demo', '')));
    openCanvas(matchedProject?.id);
  };

  return (
    <div className="agent-home">
      <Header />
      <main className={`agent-home__main agent-home__main--count-${visibleProjects.length}`}>
        <section className="agent-home__intro" aria-labelledby="agent-home-title">
          <p className="agent-home__welcome">
            {isNewUser ? '欢迎来到 Luminova' : `回来啦，${displayName}`}
          </p>
          <h1 id="agent-home-title">
            {isNewUser ? '第一支作品，想拍点什么？' : '今天想继续什么？'}
          </h1>
          <p className="agent-home__lead">
            {isNewUser
              ? '一句话、一个脑洞，剩下的交给 Agent 和画布。'
              : '继续上次的灵感，翻翻其他画布，或者开个新坑。'}
          </p>

          <form className="agent-composer" onSubmit={handleSubmit}>
            <label className="sr-only" htmlFor="agent-prompt">
              告诉 Agent 你想继续或创作什么
            </label>
            <textarea
              id="agent-prompt"
              value={prompt}
              onChange={(event) => setPrompt(event.target.value)}
              placeholder={
                isNewUser
                  ? '例如：做一支 30 秒的雨夜短片，女孩与机器人相遇…'
                  : '告诉 Agent，今天接着做哪个？'
              }
              rows={3}
            />
            <div className="agent-composer__toolbar">
              <div className="agent-composer__tools">
                <button type="button">
                  <Paperclip aria-hidden="true" />
                  添加素材
                </button>
                <button type="button">
                  <ImagePlus aria-hidden="true" />
                  参考图
                </button>
                <button type="button">
                  <Mic aria-hidden="true" />
                  语音输入
                </button>
              </div>
              <button
                className="agent-composer__send"
                type="submit"
                aria-label="发送给 Agent"
                disabled={!prompt.trim()}
              >
                <Send aria-hidden="true" />
              </button>
            </div>
          </form>

          <div className="agent-home__prompts" aria-label="试试这样说">
            {promptSuggestions.map((suggestion) => (
              <button type="button" key={suggestion} onClick={() => setPrompt(suggestion)}>
                {suggestion}
                <ArrowRight aria-hidden="true" />
              </button>
            ))}
          </div>
        </section>

        {isNewUser ? (
          <section className="agent-empty" aria-label="暂无创作">
            <span>
              <Sparkles aria-hidden="true" />
            </span>
            <p>你的作品宇宙，会从这里慢慢长出来。</p>
            <div>
              <Link to="/canvas">了解画布创作</Link>
              <button type="button" onClick={() => setPrompt('先开张白纸')}>
                先开张白纸
              </button>
            </div>
          </section>
        ) : (
          <>
            <section className="agent-project-section agent-project-section--recent">
              <div className="agent-section-heading">
                <div>
                  <span>上次拍到这里</span>
                  <h2>最近创作</h2>
                </div>
              </div>

              <article className="agent-recent-project">
                <img src={recentProject.image} alt="" />
                <div className="agent-recent-project__copy">
                  <span className="agent-project-kicker">
                    <Sparkles aria-hidden="true" />
                    接着来
                  </span>
                  <h3>{recentProject.title}</h3>
                  <p>{recentProject.meta}</p>
                  <strong>{recentProject.detail}</strong>
                </div>
                {recentProject.progress !== undefined ? (
                  <div className="agent-project-progress" aria-label={`项目进度 ${recentProject.progress}%`}>
                    <span>已经拍到</span>
                    <div>
                      <i style={{ width: `${recentProject.progress}%` }} />
                    </div>
                    <b>{recentProject.progress}%</b>
                  </div>
                ) : null}
                <button type="button" onClick={() => openCanvas(recentProject.id)}>
                  继续创作
                  <ArrowRight aria-hidden="true" />
                </button>
              </article>
            </section>

            {otherProjects.length > 0 ? (
              <section className="agent-project-section agent-project-section--others">
                <div className="agent-section-heading">
                  <div>
                    <span>换个片场也行</span>
                    <h2>其他灵感现场</h2>
                  </div>
                  <Link to="/workspace">查看全部</Link>
                </div>
                <div
                  className={`agent-other-projects agent-other-projects--count-${otherProjects.length}`}
                >
                  {otherProjects.map((project) => (
                    <article className="agent-other-project" key={project.id}>
                      <img src={project.image} alt="" />
                      <div>
                        <h3>{project.title}</h3>
                        <p>{project.meta}</p>
                        <span>{project.detail}</span>
                      </div>
                      <button type="button" onClick={() => openCanvas(project.id)} aria-label={`打开${project.title}`}>
                        打开
                        <ArrowRight aria-hidden="true" />
                      </button>
                    </article>
                  ))}
                </div>
              </section>
            ) : null}
          </>
        )}

        <footer className="agent-home__footer">
          <Link to="/workspace">
            全部项目
            <ArrowRight aria-hidden="true" />
            <span>工作台</span>
          </Link>
          <i />
          <Link to="/workspace">
            全部画布
            <ArrowRight aria-hidden="true" />
            <span>画布</span>
          </Link>
        </footer>
      </main>
    </div>
  );
}
