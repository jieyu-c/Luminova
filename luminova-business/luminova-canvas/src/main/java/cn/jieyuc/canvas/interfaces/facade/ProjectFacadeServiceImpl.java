package cn.jieyuc.canvas.interfaces.facade;

import cn.hutool.core.util.StrUtil;
import cn.jieyuc.canvas.application.service.project.ProjectApplicationService;
import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import cn.jieyuc.canvas.domain.model.project.ProjectType;
import cn.jieyuc.canvas.interfaces.converter.ProjectInterfaceConverter;
import cn.jieyuc.luminova.api.canvas.request.ProjectCreateRequest;
import cn.jieyuc.luminova.api.canvas.request.ProjectPageRequest;
import cn.jieyuc.luminova.api.canvas.response.ProjectOperateResponse;
import cn.jieyuc.luminova.api.canvas.response.ProjectPageResponse;
import cn.jieyuc.luminova.api.canvas.service.ProjectFacadeService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService(version = "v1.0.0")
public class ProjectFacadeServiceImpl implements ProjectFacadeService {

    @Autowired
    private ProjectApplicationService projectApplicationService;

    @Override
    public ProjectOperateResponse createProject(ProjectCreateRequest request) {
        if (request == null || request.getOwnerId() == null || StrUtil.isBlank(request.getName())) {
            return null;
        }
        CreativeProject project = projectApplicationService.createProject(
                request.getOwnerId(),
                request.getName(),
                ProjectType.of(request.getProjectType())
        );
        ProjectOperateResponse response = new ProjectOperateResponse();
        response.setProjectInfo(ProjectInterfaceConverter.toApiInfo(project));
        return response;
    }

    @Override
    public ProjectPageResponse pageProjects(ProjectPageRequest request) {
        if (request == null || request.getOwnerId() == null) {
            return null;
        }
        IPage<CreativeProject> projectPage = projectApplicationService.pageProjects(
                request.getOwnerId(),
                ProjectStatus.ofName(request.getStatus()),
                request.getPageNo() == null ? 1L : request.getPageNo(),
                request.getPageSize() == null ? 20L : request.getPageSize()
        );
        ProjectPageResponse response = new ProjectPageResponse();
        response.setPageNo(projectPage.getCurrent());
        response.setPageSize(projectPage.getSize());
        response.setTotal(projectPage.getTotal());
        response.setPages(projectPage.getPages());
        response.setRecords(projectPage.getRecords().stream()
                .map(ProjectInterfaceConverter::toApiInfo)
                .toList());
        return response;
    }
}
