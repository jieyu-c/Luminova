package cn.jieyuc.luminova.api.canvas.service;

import cn.jieyuc.luminova.api.canvas.request.ProjectCreateRequest;
import cn.jieyuc.luminova.api.canvas.request.ProjectPageRequest;
import cn.jieyuc.luminova.api.canvas.response.ProjectOperateResponse;
import cn.jieyuc.luminova.api.canvas.response.ProjectPageResponse;

public interface ProjectFacadeService {
    ProjectOperateResponse createProject(ProjectCreateRequest request);

    ProjectPageResponse pageProjects(ProjectPageRequest request);
}
