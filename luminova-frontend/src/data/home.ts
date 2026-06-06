import {
  Bot,
  Clapperboard,
  GitBranch,
  Layers3,
  type LucideIcon,
} from 'lucide-react';

export type FlowNode = {
  kind: 'script' | 'role' | 'shot' | 'prompt' | 'scene' | 'video';
  title: string;
  detail: string;
};

export type Highlight = {
  label: string;
  title: string;
  text: string;
};

export type Capability = {
  icon: LucideIcon;
  title: string;
  text: string;
};

export type ShowcaseItem = {
  title: string;
  tags: string[];
};

export const flowNodes: FlowNode[] = [
  { kind: 'script', title: '剧本', detail: '开场 / 反转' },
  { kind: 'role', title: '角色', detail: '林夏 · 已锁定' },
  { kind: 'shot', title: '分镜 03', detail: '近景推镜' },
  { kind: 'prompt', title: 'Prompt', detail: '角色 + 运镜' },
  { kind: 'scene', title: '场景', detail: '雨夜便利店' },
  { kind: 'video', title: '视频 V3', detail: '首选版本' },
];

export const highlights: Highlight[] = [
  {
    label: 'Canvas-first',
    title: '把创意摊开，而不是塞进聊天框',
    text: '剧本、角色、场景、分镜与素材以节点方式并排生长，复杂短片也能被看见、调整和复用。',
  },
  {
    label: 'Consistent Story',
    title: '角色、风格与镜头语言持续一致',
    text: '每个生成结果都带着明确上下文，角色设定、场景氛围和 Prompt 资产可以跨镜头继承。',
  },
  {
    label: 'Agent Co-pilot',
    title: '让 Agent 做重复劳动，创作者保留控制权',
    text: '自动拆分分镜、补全提示词、批量生成变体；关键参数和版本选择仍然掌握在你手里。',
  },
];

export const capabilities: Capability[] = [
  {
    icon: GitBranch,
    title: '节点化工作流',
    text: '剧本到视频全链路可视化编排，依赖关系一目了然。',
  },
  {
    icon: Bot,
    title: 'Agent 批量助手',
    text: '分镜拆解、Prompt 生成、缺失项检查，减少重复劳动。',
  },
  {
    icon: Layers3,
    title: '版本可追溯',
    text: '每次生成保留参数与上下文，支持对比与回滚。',
  },
  {
    icon: Clapperboard,
    title: '专业可控',
    text: '面向短剧、广告、漫剧团队，而非一键玩具流。',
  },
];

export const showcase: ShowcaseItem[] = [
  {
    title: '雨夜悬疑短剧',
    tags: ['8 分镜', '角色一致性', '4 版本'],
  },
  {
    title: '新品广告预演',
    tags: ['16:9', '电影光'],
  },
  {
    title: '漫剧镜头库',
    tags: ['批量生成', '版本对比'],
  },
  {
    title: '口播剧情 A/B',
    tags: ['口播', '9:16', 'A/B 测试'],
  },
];
