export type CanvasLibraryStatus = '进行中' | '待确认' | '待审核' | '草稿';

export type CanvasProject = {
  id: string;
  name: string;
  type: '通用' | '漫剧' | '广告';
  canvasCount: number;
  cover: string;
};

export type CanvasLibraryItem = {
  id: string;
  projectId: string;
  title: string;
  image: string;
  status: CanvasLibraryStatus;
  updatedAt: string;
  season?: string;
  episode?: string;
};

export const canvasProjects: CanvasProject[] = [
  {
    id: 'unfiled',
    name: '未归档画布',
    type: '通用',
    canvasCount: 3,
    cover: '/prototype/home-showcase-assets/video-output.png',
  },
  {
    id: 'rainy-store',
    name: '雨夜便利店',
    type: '通用',
    canvasCount: 8,
    cover: '/prototype/home-showcase-assets/project-rain.png',
  },
  {
    id: 'spirit-comic',
    name: '灵域漫剧',
    type: '漫剧',
    canvasCount: 10,
    cover: '/prototype/home-showcase-assets/project-anime.png',
  },
  {
    id: 'tea-ad',
    name: '茶叶广告',
    type: '广告',
    canvasCount: 6,
    cover: '/prototype/home-showcase-assets/project-ad.png',
  },
];

export const canvasLibraryItems: CanvasLibraryItem[] = [
  {
    id: 'spirit-ep3-main',
    projectId: 'spirit-comic',
    title: '第 03 集 · 主画布',
    image: '/prototype/home-showcase-assets/project-anime.png',
    status: '进行中',
    updatedAt: '3 小时前',
    season: '第一季',
    episode: '第 03 集',
  },
  {
    id: 'spirit-ep3-storyboard',
    projectId: 'spirit-comic',
    title: '第 03 集 · 分镜探索',
    image: '/prototype/home-showcase-assets/video-output.png',
    status: '待确认',
    updatedAt: '6 小时前',
    season: '第一季',
    episode: '第 03 集',
  },
  {
    id: 'spirit-ep3-character',
    projectId: 'spirit-comic',
    title: '第 03 集 · 角色一致性测试',
    image: '/prototype/home-showcase-assets/character-face.png',
    status: '待审核',
    updatedAt: '1 天前',
    season: '第一季',
    episode: '第 03 集',
  },
  {
    id: 'spirit-ep4-main',
    projectId: 'spirit-comic',
    title: '第 04 集 · 主画布',
    image: '/prototype/home-showcase-assets/project-anime.png',
    status: '待确认',
    updatedAt: '2 小时前',
    season: '第一季',
    episode: '第 04 集',
  },
  {
    id: 'spirit-ep4-storyboard',
    projectId: 'spirit-comic',
    title: '第 04 集 · 分镜探索',
    image: '/prototype/home-showcase-assets/project-rain.png',
    status: '进行中',
    updatedAt: '5 小时前',
    season: '第一季',
    episode: '第 04 集',
  },
  {
    id: 'spirit-ep4-character',
    projectId: 'spirit-comic',
    title: '第 04 集 · 角色一致性测试',
    image: '/prototype/home-showcase-assets/character-face.png',
    status: '草稿',
    updatedAt: '1 天前',
    season: '第一季',
    episode: '第 04 集',
  },
  {
    id: 'spirit-ep2-atmosphere',
    projectId: 'spirit-comic',
    title: '第 02 集 · 氛围测试',
    image: '/prototype/home-showcase-assets/project-rain.png',
    status: '进行中',
    updatedAt: '2 天前',
    season: '第一季',
    episode: '第 02 集',
  },
  {
    id: 'spirit-ep2-hero',
    projectId: 'spirit-comic',
    title: '第 02 集 · 男主一致性测试',
    image: '/prototype/home-showcase-assets/character-face.png',
    status: '待审核',
    updatedAt: '2 天前',
    season: '第一季',
    episode: '第 02 集',
  },
  {
    id: 'spirit-ep1-color',
    projectId: 'spirit-comic',
    title: '第 01 集 · 配色版本',
    image: '/prototype/home-showcase-assets/project-anime.png',
    status: '待确认',
    updatedAt: '3 天前',
    season: '第一季',
    episode: '第 01 集',
  },
  {
    id: 'rainy-main',
    projectId: 'rainy-store',
    title: '雨夜便利店 · 主画布',
    image: '/prototype/home-showcase-assets/project-rain.png',
    status: '进行中',
    updatedAt: '刚刚',
  },
  {
    id: 'rainy-atmosphere',
    projectId: 'rainy-store',
    title: '氛围测试 · 雨夜街景',
    image: '/prototype/home-showcase-assets/video-output.png',
    status: '进行中',
    updatedAt: '2 天前',
  },
  {
    id: 'tea-main',
    projectId: 'tea-ad',
    title: '茶叶广告 · 主画布',
    image: '/prototype/home-showcase-assets/project-ad.png',
    status: '待审核',
    updatedAt: '4 小时前',
  },
  {
    id: 'unfiled-concept',
    projectId: 'unfiled',
    title: '机械舞者概念探索',
    image: '/prototype/home-showcase-assets/video-output.png',
    status: '草稿',
    updatedAt: '3 天前',
  },
];
