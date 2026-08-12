import { apiFetch } from './client';
import type {
  ProjectInfoResponse,
  ProjectPageResponse,
  ProjectStatus,
  ProjectType,
} from './types';

export type CreateProjectPayload = {
  name: string;
  projectType: ProjectType;
};

export type PageProjectsParams = {
  status?: ProjectStatus;
  pageNo?: number;
  pageSize?: number;
};

export function createProject(data: CreateProjectPayload) {
  return apiFetch<ProjectInfoResponse>('/api/v1/projects', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function pageProjects(params: PageProjectsParams = {}) {
  const searchParams = new URLSearchParams();

  if (params.status) searchParams.set('status', params.status);
  if (params.pageNo) searchParams.set('pageNo', String(params.pageNo));
  if (params.pageSize) searchParams.set('pageSize', String(params.pageSize));

  const query = searchParams.toString();
  return apiFetch<ProjectPageResponse>(`/api/v1/projects${query ? `?${query}` : ''}`);
}
