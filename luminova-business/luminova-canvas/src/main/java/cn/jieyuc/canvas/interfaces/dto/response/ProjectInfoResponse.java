package cn.jieyuc.canvas.interfaces.dto.response;

import cn.jieyuc.luminova.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectInfoResponse extends BaseResponse {
    private ProjectInfo projectInfo;

    public static ProjectInfoResponse from(ProjectInfo projectInfo) {
        ProjectInfoResponse response = new ProjectInfoResponse();
        response.setSuccess(true);
        response.setProjectInfo(projectInfo);
        return response;
    }
}
