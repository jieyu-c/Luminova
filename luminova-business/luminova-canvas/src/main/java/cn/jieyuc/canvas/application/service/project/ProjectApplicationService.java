package cn.jieyuc.canvas.application.service.project;

import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import cn.jieyuc.canvas.domain.model.project.ProjectType;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface ProjectApplicationService {
    CreativeProject createProject(Long ownerId, String projectName, ProjectType projectType);

    IPage<CreativeProject> pageProjects(Long ownerId, ProjectStatus status, long pageNo, long pageSize);
}
