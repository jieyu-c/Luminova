package cn.jieyuc.canvas.application.service.project.impl;

import cn.jieyuc.canvas.application.service.project.ProjectApplicationService;
import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import cn.jieyuc.canvas.domain.model.project.ProjectType;
import cn.jieyuc.canvas.domain.repo.CreativeProjectRepo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProjectApplicationServiceImpl implements ProjectApplicationService {
    @Autowired
    private CreativeProjectRepo creativeProjectRepo;

    @Override
    public CreativeProject createProject(Long ownerId, String projectName, ProjectType projectType) {
        CreativeProject project = CreativeProject.create(ownerId, projectName, projectType);
        return creativeProjectRepo.save(project);
    }

    @Override
    public IPage<CreativeProject> pageProjects(Long ownerId, ProjectStatus status, long pageNo, long pageSize) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Project owner must not be null");
        }
        long normalizedPageNo = Math.max(pageNo, 1);
        long normalizedPageSize = Math.clamp(pageSize, 1, 100);
        return creativeProjectRepo.pageByOwnerId(ownerId, status, normalizedPageNo, normalizedPageSize);
    }
}
