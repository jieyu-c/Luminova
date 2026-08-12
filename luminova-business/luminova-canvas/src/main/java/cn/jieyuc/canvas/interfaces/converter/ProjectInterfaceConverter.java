package cn.jieyuc.canvas.interfaces.converter;

import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectId;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import cn.jieyuc.canvas.domain.model.project.ProjectType;
import cn.jieyuc.canvas.interfaces.dto.response.ProjectInfo;

public final class ProjectInterfaceConverter {

    private ProjectInterfaceConverter() {
    }

    public static ProjectInfo toRestInfo(CreativeProject project) {
        if (project == null) {
            return null;
        }
        ProjectInfo projectInfo =
                new ProjectInfo();
        fill(project, projectInfo);
        return projectInfo;
    }

    public static cn.jieyuc.luminova.api.canvas.response.data.ProjectInfo toApiInfo(CreativeProject project) {
        if (project == null) {
            return null;
        }
        cn.jieyuc.luminova.api.canvas.response.data.ProjectInfo projectInfo =
                new cn.jieyuc.luminova.api.canvas.response.data.ProjectInfo();
        fill(project, projectInfo);
        return projectInfo;
    }

    private static void fill(CreativeProject project, cn.jieyuc.canvas.interfaces.dto.response.ProjectInfo projectInfo) {
        projectInfo.setProjectId(projectIdValue(project.getId()));
        projectInfo.setOwnerId(project.getOwnerId());
        projectInfo.setName(project.getName());
        projectInfo.setProjectType(projectTypeName(project.getProjectType()));
        projectInfo.setDescription(project.getDescription());
        projectInfo.setCoverUrl(project.getCoverUrl());
        projectInfo.setAspectRatio(project.getAspectRatio());
        projectInfo.setTargetDurationMs(project.getTargetDurationMs());
        projectInfo.setStatus(statusName(project.getStatus()));
        projectInfo.setCreatedAt(project.getCreatedAt());
        projectInfo.setUpdatedAt(project.getUpdatedAt());
    }

    private static void fill(CreativeProject project, cn.jieyuc.luminova.api.canvas.response.data.ProjectInfo projectInfo) {
        projectInfo.setProjectId(projectIdValue(project.getId()));
        projectInfo.setOwnerId(project.getOwnerId());
        projectInfo.setName(project.getName());
        projectInfo.setProjectType(projectTypeName(project.getProjectType()));
        projectInfo.setDescription(project.getDescription());
        projectInfo.setCoverUrl(project.getCoverUrl());
        projectInfo.setAspectRatio(project.getAspectRatio());
        projectInfo.setTargetDurationMs(project.getTargetDurationMs());
        projectInfo.setStatus(statusName(project.getStatus()));
        projectInfo.setCreatedAt(project.getCreatedAt());
        projectInfo.setUpdatedAt(project.getUpdatedAt());
    }

    private static Long projectIdValue(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }

    private static String projectTypeName(ProjectType projectType) {
        return projectType == null ? null : projectType.name();
    }

    private static String statusName(ProjectStatus status) {
        return status == null ? null : status.name();
    }
}
