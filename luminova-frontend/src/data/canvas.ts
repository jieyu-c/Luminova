import type { Edge, Node } from '@xyflow/react';

export type CanvasNodeKind =
  | 'shot'
  | 'character'
  | 'sceneRef'
  | 'dialogue'
  | 'styleRef'
  | 'shotContext';

export type ShotContextItem = {
  label: string;
  image: string;
};

export type CanvasNodeData = {
  title: string;
  label: string;
  kind: CanvasNodeKind;
  status: 'ready' | 'running' | 'draft';
  prompt?: string;
  duration?: string;
  camera?: string;
  shotSize?: string;
  previewImage?: string;
  avatar?: string;
  palette?: string[];
  image?: string;
  dialogue?: string;
  timeRange?: string;
  prevShot?: ShotContextItem;
  nextShot?: ShotContextItem;
};

export type CanvasNode = Node<CanvasNodeData, 'luminovaNode'>;
export type CanvasEdge = Edge<{ label?: string }>;

export type CanvasShot = {
  id: string;
  label: string;
  title: string;
  duration: string;
  image: string;
  active?: boolean;
};

export const canvasShots: CanvasShot[] = [
  {
    id: 'shot-01',
    label: '01',
    title: '雨夜街道',
    duration: '4.0s',
    image: '/prototype/home-showcase-assets/project-rain.png',
  },
  {
    id: 'shot-02',
    label: '02',
    title: '便利店门口',
    duration: '5.0s',
    image: '/prototype/home-showcase-assets/project-rain.png',
  },
  {
    id: 'shot-03',
    label: '03',
    title: '货架近景',
    duration: '6.0s',
    image: '/prototype/home-showcase-assets/video-output.png',
    active: true,
  },
  {
    id: 'shot-04',
    label: '04',
    title: '录像回放',
    duration: '5.0s',
    image: '/prototype/home-showcase-assets/project-rain.png',
  },
  {
    id: 'shot-05',
    label: '05',
    title: '反转结尾',
    duration: '6.0s',
    image: '/prototype/home-showcase-assets/project-anime.png',
  },
];

export const canvasToolboxItems = [
  { id: 'assets', label: '素材' },
  { id: 'character', label: '角色' },
  { id: 'scene', label: '场景' },
  { id: 'style', label: '风格' },
  { id: 'dialogue', label: '对白' },
  { id: 'shot', label: '镜头' },
  { id: 'music', label: '音乐' },
  { id: 'effects', label: '特效' },
  { id: 'text', label: '文字' },
] as const;

const makeNode = (
  id: string,
  data: CanvasNodeData,
  position: CanvasNode['position'],
): CanvasNode => ({
  id,
  type: 'luminovaNode',
  position,
  data,
});

export const initialCanvasNodes: CanvasNode[] = [
  makeNode(
    'shot-03',
    {
      kind: 'shot',
      label: '镜头',
      title: '镜头 03 · 便利店雨夜',
      status: 'running',
      prompt:
        '雨夜便利店，侦探推门而入，货架反光，冷色霓虹，电影感侧光，湿润地面反射，悬疑氛围。',
      duration: '6.0s',
      camera: '缓慢推进',
      shotSize: '中景',
      previewImage: '/prototype/home-showcase-assets/video-output.png',
    },
    { x: 400, y: 40 },
  ),
  makeNode(
    'character-1',
    {
      kind: 'character',
      label: '角色',
      title: '失忆侦探',
      status: 'ready',
      avatar: '/prototype/home-showcase-assets/character-face.png',
      palette: ['#1e3a5f', '#334155', '#64748b', '#94a3b8'],
    },
    { x: 40, y: 60 },
  ),
  makeNode(
    'scene-ref-1',
    {
      kind: 'sceneRef',
      label: '场景参考',
      title: '雨夜便利店',
      status: 'ready',
      image: '/prototype/home-showcase-assets/project-rain.png',
      palette: ['#0f172a', '#1e40af', '#06b6d4', '#f8fafc'],
    },
    { x: 40, y: 300 },
  ),
  makeNode(
    'dialogue-1',
    {
      kind: 'dialogue',
      label: '对白片段',
      title: '对白片段',
      status: 'ready',
      dialogue: '「这段录像……好像来自明天。」',
      timeRange: '00:04 – 00:08',
    },
    { x: 40, y: 520 },
  ),
  makeNode(
    'style-ref-1',
    {
      kind: 'styleRef',
      label: '风格参考',
      title: '霓虹雨街',
      status: 'ready',
      image: '/prototype/home-showcase-assets/project-rain.png',
    },
    { x: 900, y: 60 },
  ),
  makeNode(
    'shot-context-1',
    {
      kind: 'shotContext',
      label: '镜头上下文',
      title: '上一镜头 / 下一镜头',
      status: 'ready',
      prevShot: {
        label: '镜头 02',
        image: '/prototype/home-showcase-assets/project-rain.png',
      },
      nextShot: {
        label: '镜头 04',
        image: '/prototype/home-showcase-assets/project-anime.png',
      },
    },
    { x: 900, y: 340 },
  ),
];

export const initialCanvasEdges: CanvasEdge[] = [
  { id: 'character-shot', source: 'character-1', target: 'shot-03', label: '角色约束' },
  { id: 'scene-shot', source: 'scene-ref-1', target: 'shot-03', label: '场景参考' },
  { id: 'dialogue-shot', source: 'dialogue-1', target: 'shot-03', label: '对白' },
  { id: 'shot-style', source: 'shot-03', target: 'style-ref-1', label: '风格继承' },
  { id: 'shot-context', source: 'shot-03', target: 'shot-context-1', label: '连续性' },
];
