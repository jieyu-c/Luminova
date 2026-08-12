import { useCallback, useEffect, useMemo, useState } from 'react';
import { createProject, pageProjects } from '../lib/api/project';
import { ApiError } from '../lib/api/client';
import type { ProjectInfo, ProjectType } from '../lib/api/types';
import type { ProjectStatus as WorkspaceProjectStatus, TableProject } from '../data/workspace';

const PROJECT_TYPE_LABELS: Record<ProjectType, string> = {
  GENERAL: '通用',
  SHORT_DRAMA: '短剧',
  COMIC: '漫剧',
  AD: '广告',
  TALKING_HEAD: '口播',
};

const PROJECT_TYPE_VALUES: Record<string, ProjectType> = {
  通用: 'GENERAL',
  短剧: 'SHORT_DRAMA',
  漫剧: 'COMIC',
  广告: 'AD',
  口播: 'TALKING_HEAD',
};

const STATUS_LABELS: Record<ProjectInfo['status'], WorkspaceProjectStatus> = {
  DRAFT: '草稿',
  IN_PROGRESS: '进行中',
  PENDING_REVIEW: '待审核',
  COMPLETED: '已完成',
};

const STATUS_PROGRESS: Record<ProjectInfo['status'], number> = {
  DRAFT: 8,
  IN_PROGRESS: 42,
  PENDING_REVIEW: 86,
  COMPLETED: 100,
};

const ACCENTS = ['var(--blue)', 'var(--violet)', 'var(--cyan)', 'var(--node-script)'];

function formatDuration(durationMs?: number | null) {
  if (!durationMs) return '-';
  const seconds = Math.round(durationMs / 1000);
  return `${seconds}s`;
}

function formatRelativeTime(value?: string | null) {
  if (!value) return '刚刚';

  const timestamp = new Date(value).getTime();
  if (Number.isNaN(timestamp)) return '刚刚';

  const diffMs = Date.now() - timestamp;
  if (diffMs < 60_000) return '刚刚';

  const minutes = Math.floor(diffMs / 60_000);
  if (minutes < 60) return `${minutes} 分钟前`;

  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} 小时前`;

  const days = Math.floor(hours / 24);
  if (days < 7) return `${days} 天前`;

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  }).format(timestamp);
}

function toTableProject(project: ProjectInfo, index: number): TableProject {
  const id = String(project.projectId);

  return {
    id,
    title: project.name,
    type: PROJECT_TYPE_LABELS[project.projectType] ?? '通用',
    aspectRatio: project.aspectRatio ?? '-',
    duration: formatDuration(project.targetDurationMs),
    progress: STATUS_PROGRESS[project.status] ?? 0,
    status: STATUS_LABELS[project.status] ?? '草稿',
    updatedAt: formatRelativeTime(project.updatedAt ?? project.createdAt),
    accent: ACCENTS[index % ACCENTS.length],
  };
}

export function toProjectType(value: string): ProjectType {
  return PROJECT_TYPE_VALUES[value] ?? 'GENERAL';
}

export function useWorkspaceProjects({ enabled = true }: { enabled?: boolean } = {}) {
  const [projects, setProjects] = useState<TableProject[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshProjects = useCallback(async () => {
    if (!enabled) {
      setProjects([]);
      setIsLoading(false);
      setError(null);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const response = await pageProjects({ pageNo: 1, pageSize: 20 });
      setProjects((response.records ?? []).map(toTableProject));
    } catch (err) {
      const message = err instanceof ApiError ? err.message : '项目列表加载失败';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [enabled]);

  useEffect(() => {
    void refreshProjects();
  }, [refreshProjects]);

  const createWorkspaceProject = useCallback(async (data: { name: string; projectType: ProjectType }) => {
    if (!enabled) {
      throw new ApiError('请先登录后再创建项目', 401, 'UNAUTHORIZED');
    }

    setIsCreating(true);
    setError(null);

    try {
      const response = await createProject(data);
      if (!response.projectInfo) {
        throw new ApiError('项目创建成功，但响应缺少项目信息', 200);
      }

      const createdProject = toTableProject(response.projectInfo, 0);
      setProjects((current) => [
        createdProject,
        ...current.filter((project) => project.id !== createdProject.id),
      ]);
      return createdProject;
    } catch (err) {
      const message = err instanceof ApiError ? err.message : '项目创建失败';
      setError(message);
      throw err;
    } finally {
      setIsCreating(false);
    }
  }, [enabled]);

  const stats = useMemo(
    () => ({
      activeTasks: projects.filter((project) => project.status === '进行中').length,
      pendingReview: projects.filter((project) => project.status === '待审核').length,
    }),
    [projects],
  );

  return {
    projects,
    currentProject: projects[0] ?? null,
    stats,
    isLoading,
    isCreating,
    error,
    refreshProjects,
    createWorkspaceProject,
  };
}
