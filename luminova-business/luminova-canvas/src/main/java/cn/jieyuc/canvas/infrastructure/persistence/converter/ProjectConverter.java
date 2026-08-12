package cn.jieyuc.canvas.infrastructure.persistence.converter;

import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectId;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import cn.jieyuc.canvas.domain.model.project.ProjectType;
import cn.jieyuc.canvas.infrastructure.persistence.entity.CreativeProjectPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectConverter {
    ProjectConverter INSTANCE = Mappers.getMapper(ProjectConverter.class);

    CreativeProject toDomain(CreativeProjectPO projectPO);

    CreativeProjectPO toPO(CreativeProject project);

    default ProjectId map(Long id) {
        return id == null ? null : ProjectId.of(id);
    }

    default Long map(ProjectId id) {
        return id == null ? null : id.value();
    }

    default ProjectType mapProjectType(String value) {
        return ProjectType.of(value);
    }

    default String map(ProjectType projectType) {
        return projectType == null ? null : projectType.name();
    }

    default ProjectStatus map(Short value) {
        return ProjectStatus.of(value);
    }

    default Short map(ProjectStatus status) {
        return status == null ? null : status.code();
    }
}
