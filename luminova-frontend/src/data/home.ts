import { brandAssets } from './brand';

export type MetricItem = {
  value: string;
  label: string;
  desc: string;
};

export type ValueItem = {
  title: string;
  text: string;
};

export type CaseItem = {
  title: string;
  desc: string;
  tags: string[];
  image: string;
};

export type AgentStep = {
  label: string;
  status: 'done' | 'active' | 'pending';
};

export const heroTrustChips = ['Canvas-first', 'Agent co-pilot', '角色一致性'];

export const heroAgentDefaultPrompt =
  '雨夜便利店里的未来录像：主角在雨夜寻找失踪姐姐，镜头要有霓虹反射与情绪反转。';

export const heroAgentQuickActions = [
  {
    label: '拆剧本',
    prompt: '雨夜便利店短剧：请拆解剧本结构，输出场次、人物关系与关键镜头。',
  },
  {
    label: '建画布',
    prompt: '雨夜便利店短剧：根据剧本自动生成节点画布，包含角色、场景与分镜。',
  },
  {
    label: '出首版',
    prompt: '雨夜便利店短剧：基于现有画布生成首版分镜与 30 秒视频片段。',
  },
] as const;

export const heroIdeaSteps = heroAgentQuickActions.map((item) => item.label);

export const agentChecklist = [
  { label: '拆解剧本结构', status: 'done' },
  { label: '锁定角色一致性', status: 'done' },
  { label: '生成分镜候选', status: 'active' },
  { label: '检查缺失素材', status: 'pending' },
] as const;

export const capabilityItems = [
  {
    index: '01',
    title: '节点化工作流',
    text: '剧本到视频全链路可视化编排，依赖关系一目了然。',
  },
  {
    index: '02',
    title: 'Agent 批量助手',
    text: '自动拆解、补全 Prompt、检查缺失项，减少重复劳动。',
  },
  {
    index: '03',
    title: '版本可追溯',
    text: '每次生成保留参数与上下文，支持对比与回滚。',
  },
  {
    index: '04',
    title: '专业可控',
    text: '面向短剧、广告、漫剧团队，而不是一键玩具流。',
  },
] as const;

export const metrics: MetricItem[] = [
  {
    value: '10min',
    label: '首版片段',
    desc: '从灵感到可用的视频片段',
  },
  {
    value: '7类',
    label: '创作节点',
    desc: '覆盖剧本到成片的完整链路',
  },
  {
    value: '4种',
    label: '资产继承',
    desc: '角色 / 场景 / 风格 / Prompt 继承',
  },
];

export const valueItems: ValueItem[] = [
  {
    title: '资产不丢失',
    text: '角色、场景、风格与 Prompt 被结构化管理，一致性在项目间延续。',
  },
  {
    title: '生成可追溯',
    text: '每一步生成都有来源与参数记录，版本演化清晰可见，随时回溯。',
  },
  {
    title: 'Agent 承担重复劳动',
    text: '自动拆解剧本、锁定角色、生成分镜与版本管理，让团队专注创意判断。',
  },
];

export const cases: CaseItem[] = [
  {
    title: '雨夜悬疑短剧',
    desc: '复杂角色与多场景连续叙事，风格统一，版本迭代高效。',
    tags: ['悬疑', '短剧', '连续性'],
    image: '/prototype/home-showcase-assets/project-rain.png',
  },
  {
    title: '新品广告预演',
    desc: '快速验证创意与镜头方案，支持多版本对比与客户评审。',
    tags: ['广告', '产品', '分镜预演'],
    image: '/prototype/home-showcase-assets/project-ad.png',
  },
  {
    title: '漫剧镜头库',
    desc: '沉淀可复用镜头资产库，提升产能，保障画风一致。',
    tags: ['动画', '漫剧', '镜头库'],
    image: '/prototype/home-showcase-assets/project-anime.png',
  },
];

export const agentSteps: AgentStep[] = [
  { label: '拆剧本', status: 'done' },
  { label: '锁角色', status: 'done' },
  { label: '生成分镜', status: 'active' },
  { label: '导出版本', status: 'pending' },
];

export const showcaseAssets = {
  characterFace: '/prototype/home-showcase-assets/character-face.png',
  projectRain: '/prototype/home-showcase-assets/project-rain.png',
  projectAnime: '/prototype/home-showcase-assets/project-anime.png',
  videoOutput: '/prototype/home-showcase-assets/video-output.png',
  logoMark: brandAssets.logoMark,
};

export type ProductFlowNode = {
  id: string;
  type: string;
  title: string;
  detail: string;
  accent: string;
  featured?: boolean;
  image?: string;
  images?: string[];
};

export const productFlowNodes: ProductFlowNode[] = [
  {
    id: 'script',
    type: '剧本',
    title: '记忆修复师',
    detail: '第 1 集 · 雨夜开场',
    accent: 'var(--node-script)',
  },
  {
    id: 'character',
    type: '角色',
    title: '林夏 · 女主',
    detail: '一致性 92%',
    accent: 'var(--node-role)',
    image: showcaseAssets.characterFace,
  },
  {
    id: 'storyboard',
    type: '分镜',
    title: '第 03 场 · 天台对峙',
    detail: 'Prompt 已复用 12 次',
    accent: 'var(--node-shot)',
    featured: true,
    images: [
      showcaseAssets.projectRain,
      showcaseAssets.videoOutput,
      showcaseAssets.projectAnime,
      showcaseAssets.projectRain,
    ],
  },
  {
    id: 'video',
    type: '视频',
    title: '版本 v2.4',
    detail: '4K · 24fps · 02:15',
    accent: 'var(--node-video)',
    image: showcaseAssets.videoOutput,
  },
];
