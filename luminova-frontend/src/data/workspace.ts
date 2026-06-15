export type Project = {
  id: string;
  title: string;
  tags: string[];
  progress: number;
  accent: string;
};

export type ProjectStatus = '进行中' | '待审核' | '已完成' | '草稿';

export type WorkflowStepStatus = 'done' | 'active' | 'warning' | 'pending';

export type WorkflowStep = {
  id: string;
  label: string;
  status: WorkflowStepStatus;
  statusLabel?: string;
};

export type ProjectTaskStatus = 'pending' | 'in-progress' | 'pending-review';

export type ProjectTask = {
  id: string;
  index: number;
  title: string;
  description: string;
  assignees: string[];
  status: ProjectTaskStatus;
  statusLabel: string;
};

export type TableProject = {
  id: string;
  title: string;
  type: string;
  aspectRatio: string;
  duration: string;
  progress: number;
  status: ProjectStatus;
  updatedAt: string;
  accent: string;
};

export type MissingAsset = {
  id: string;
  name: string;
};

export type RecommendedTemplate = {
  id: string;
  title: string;
  tags: string[];
  usageCount: number;
  accent: string;
};

export type ComposerOption = {
  label: string;
  value: string;
};

export const workspaceStats = {
  activeTasks: 5,
  pendingReview: 2,
};

export const currentProject = {
  id: 'rainy-store',
  title: '雨夜便利店',
  progress: 68,
  nextStep: '分镜 Prompt 待确认',
  status: '进行中' as ProjectStatus,
  accent: 'var(--blue)',
};

export const workflowSteps: WorkflowStep[] = [
  { id: 'script', label: '剧本解析', status: 'done' },
  { id: 'role', label: '角色设定', status: 'done' },
  { id: 'scene', label: '场景库', status: 'done' },
  { id: 'storyboard', label: '分镜', status: 'active', statusLabel: '进行中' },
  { id: 'prompt', label: 'Prompt', status: 'warning', statusLabel: '待确认' },
  { id: 'video', label: '视频结果', status: 'pending', statusLabel: '待生成' },
];

export const projectTasks: ProjectTask[] = [
  {
    id: 'task-prompt-12-16',
    index: 1,
    title: '确认镜头 12-16 的 Prompt',
    description: '在图像生成前审核并锁定该镜头区间的视觉 Prompt。',
    assignees: ['林', '陈'],
    status: 'pending',
    statusLabel: 'Pending',
  },
  {
    id: 'task-character-20',
    index: 2,
    title: '校准镜头 20 的角色一致性',
    description: '确保天台场景中角色外观与已建立视觉参考一致。',
    assignees: ['王'],
    status: 'in-progress',
    statusLabel: 'In Progress',
  },
  {
    id: 'task-storyboard-3-5',
    index: 3,
    title: '审核镜头 3-5 的新分镜',
    description: '检查开场序列的机位角度与构图。',
    assignees: ['林', '赵', '周'],
    status: 'pending-review',
    statusLabel: 'Pending Review',
  },
];

export const composerDefaults = {
  prompt:
    '帮我做一支 30 秒赛博国风广告：一个机械舞者在古城雨巷中唤醒发光茶叶，节奏要有反转。',
  videoType: '广告',
  aspectRatio: '9:16',
  duration: '30s',
  style: '赛博国风',
};

export const composerOptions = {
  videoTypes: ['广告', '短剧', '漫剧', '口播'],
  aspectRatios: ['9:16', '16:9', '1:1'],
  durations: ['15s', '30s', '60s'],
  styles: ['赛博国风', '电影感', '日系动漫', '写实纪录片'],
};

export const tableProjects: TableProject[] = [
  {
    id: 'tea-ad',
    title: '茶叶广告 Demo',
    type: '广告',
    aspectRatio: '16:9',
    duration: '30s',
    progress: 42,
    status: '进行中',
    updatedAt: '2 小时前',
    accent: 'var(--violet)',
  },
  {
    id: 'campus-ep3',
    title: '校园漫剧第三集',
    type: '漫剧',
    aspectRatio: '1:1',
    duration: '60s',
    progress: 81,
    status: '待审核',
    updatedAt: '昨天',
    accent: 'var(--cyan)',
  },
  {
    id: 'voice-ab',
    title: '口播剧情 A/B',
    type: '口播',
    aspectRatio: '9:16',
    duration: '15s',
    progress: 55,
    status: '进行中',
    updatedAt: '3 天前',
    accent: 'var(--node-script)',
  },
];

/** @deprecated Use tableProjects for the workspace table view. */
export const recentProjects: Project[] = [
  {
    id: 'rainy-store',
    title: '雨夜便利店',
    tags: ['短剧', '9:16', '23 节点'],
    progress: 68,
    accent: 'var(--blue)',
  },
  {
    id: 'tea-ad',
    title: '茶叶广告 Demo',
    tags: ['广告', '16:9', '17 节点'],
    progress: 42,
    accent: 'var(--violet)',
  },
  {
    id: 'campus-ep3',
    title: '校园漫剧第三集',
    tags: ['漫剧', '1:1', '41 节点'],
    progress: 81,
    accent: 'var(--cyan)',
  },
  {
    id: 'voice-ab',
    title: '口播剧情 A/B',
    tags: ['口播', '9:16', '15 节点'],
    progress: 55,
    accent: 'var(--node-script)',
  },
];

export const agentSuggestion =
  '建议在场景 3 中加强雨滴与霓虹招牌的反射细节，以强化雨夜氛围。';

export const missingAssets: MissingAsset[] = [
  { id: 'rooftop-panorama', name: '天台全景素材' },
  { id: 'neon-sign', name: '霓虹招牌纹理' },
];

export const recommendedTemplates: RecommendedTemplate[] = [
  {
    id: 'cyber-alley',
    title: '赛博国风 · 霓虹巷',
    tags: ['广告', '9:16', '30s'],
    usageCount: 128,
    accent: 'linear-gradient(135deg, #6366f1, #06b6d4)',
  },
  {
    id: 'rainy-drama',
    title: '雨夜短剧模板',
    tags: ['短剧', '16:9', '60s'],
    usageCount: 96,
    accent: 'linear-gradient(135deg, #2563eb, #22c55e)',
  },
];

/** @deprecated Use missingAssets and recommendedTemplates instead. */
export const agentInsight = agentSuggestion;

/** @deprecated Use missingAssets instead. */
export const assets = missingAssets.map((item, index) => ({
  id: item.id,
  icon: index === 0 ? '景' : '材',
  iconBackground:
    index === 0
      ? 'linear-gradient(135deg,var(--cyan),var(--node-scene))'
      : 'linear-gradient(135deg,var(--blue),var(--violet))',
  name: item.name,
  detail: '缺失素材',
  type: '场景',
}));

/** @deprecated Use recommendedTemplates instead. */
export const templates = recommendedTemplates.map((item) => ({
  id: item.id,
  title: item.title,
  description: `${item.tags.join(' · ')} · ${item.usageCount} 次使用`,
}));
