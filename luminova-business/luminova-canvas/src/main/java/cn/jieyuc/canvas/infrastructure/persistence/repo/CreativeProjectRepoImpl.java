package cn.jieyuc.canvas.infrastructure.persistence.repo;

import cn.hutool.core.lang.Assert;
import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectId;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import cn.jieyuc.canvas.domain.repo.CreativeProjectRepo;
import cn.jieyuc.canvas.infrastructure.persistence.cahce.CacheProjectQuery;
import cn.jieyuc.canvas.infrastructure.persistence.converter.ProjectConverter;
import cn.jieyuc.canvas.infrastructure.persistence.entity.CreativeProjectPO;
import cn.jieyuc.canvas.infrastructure.persistence.mapper.CreativeProjectMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class CreativeProjectRepoImpl implements CreativeProjectRepo {
    @Autowired
    private CreativeProjectMapper creativeProjectMapper;

    @Autowired
    private CacheProjectQuery cacheProjectQuery;

    private final ProjectConverter converter = ProjectConverter.INSTANCE;

    @Override
    public CreativeProject save(CreativeProject project) {
        Assert.notNull(project, () -> {
            throw new IllegalArgumentException("Project must not null");
        });

        ProjectId id = project.getId();
        CreativeProjectPO existingProjectPO = id == null ? null : creativeProjectMapper.selectById(id.value());
        CreativeProjectPO projectPO = converter.toPO(project);
        if (Objects.isNull(id)) {
            // 新建
            creativeProjectMapper.insert(projectPO);
        } else {
            // 更新
            creativeProjectMapper.updateById(projectPO);
        }

        CreativeProject savedProject = converter.toDomain(projectPO);
        evictProjectCache(savedProject, converter.toDomain(existingProjectPO));
        return savedProject;
    }

    @Override
    public Optional<CreativeProject> findById(ProjectId projectId) {
        if (projectId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cacheProjectQuery.findById(projectId));
    }

    @Override
    public IPage<CreativeProject> pageByOwnerId(Long ownerId, ProjectStatus status, long pageNo, long pageSize) {
        Assert.notNull(ownerId, () -> {
            throw new IllegalArgumentException("Project owner must not null");
        });

        List<CreativeProject> projects = cacheProjectQuery.listByOwnerId(ownerId, status);
        long total = projects.size();
        long fromIndex = Math.min((pageNo - 1) * pageSize, total);
        long toIndex = Math.min(fromIndex + pageSize, total);
        List<CreativeProject> records = fromIndex >= toIndex
                ? Collections.emptyList()
                : projects.subList((int) fromIndex, (int) toIndex);

        Page<CreativeProject> projectPage = Page.of(pageNo, pageSize);
        projectPage.setTotal(total);
        projectPage.setRecords(records);
        return projectPage;
    }

    private void evictProjectCache(CreativeProject savedProject, CreativeProject existingProject) {
        if (savedProject == null) {
            return;
        }
        cacheProjectQuery.evictById(savedProject.getId());
        cacheProjectQuery.evictOwnerProjects(savedProject.getOwnerId());
        if (savedProject.getStatus() != null) {
            cacheProjectQuery.evictOwnerProjectsByStatus(savedProject.getOwnerId(), savedProject.getStatus());
        }
        if (existingProject != null
                && existingProject.getStatus() != null
                && existingProject.getStatus() != savedProject.getStatus()) {
            cacheProjectQuery.evictOwnerProjectsByStatus(existingProject.getOwnerId(), existingProject.getStatus());
        }
    }
}
