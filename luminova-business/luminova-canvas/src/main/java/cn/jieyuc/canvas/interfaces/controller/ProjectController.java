package cn.jieyuc.canvas.interfaces.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.jieyuc.canvas.application.service.project.ProjectApplicationService;
import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import cn.jieyuc.canvas.domain.model.project.ProjectType;
import cn.jieyuc.canvas.interfaces.converter.ProjectInterfaceConverter;
import cn.jieyuc.canvas.interfaces.dto.request.CreateProjectRequest;
import cn.jieyuc.canvas.interfaces.dto.response.ProjectInfo;
import cn.jieyuc.canvas.interfaces.dto.response.ProjectInfoResponse;
import cn.jieyuc.canvas.interfaces.dto.response.ProjectPageResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    @Autowired
    private ProjectApplicationService projectApplicationService;

    @SaCheckLogin
    @PostMapping
    public ProjectInfoResponse createProject(@RequestBody CreateProjectRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create project request must not be null");
        }
        Long ownerId = StpUtil.getLoginIdAsLong();
        CreativeProject project = projectApplicationService.createProject(
                ownerId,
                request.name(),
                ProjectType.of(request.projectType())
        );
        return ProjectInfoResponse.from(ProjectInterfaceConverter.toRestInfo(project));
    }

    @SaCheckLogin
    @GetMapping
    public ProjectPageResponse pageProjects(@RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "1") Long pageNo,
                                            @RequestParam(defaultValue = "20") Long pageSize) {
        Long ownerId = StpUtil.getLoginIdAsLong();
        IPage<CreativeProject> projectPage = projectApplicationService.pageProjects(
                ownerId,
                ProjectStatus.ofName(status),
                pageNo,
                pageSize
        );
        List<ProjectInfo> records = projectPage.getRecords().stream()
                .map(ProjectInterfaceConverter::toRestInfo)
                .toList();
        return ProjectPageResponse.success(
                projectPage.getCurrent(),
                projectPage.getSize(),
                projectPage.getTotal(),
                projectPage.getPages(),
                records
        );
    }
}
