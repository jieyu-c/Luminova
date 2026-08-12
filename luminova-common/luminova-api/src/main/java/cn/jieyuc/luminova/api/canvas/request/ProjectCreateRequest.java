package cn.jieyuc.luminova.api.canvas.request;

import cn.jieyuc.luminova.base.request.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectCreateRequest extends BaseRequest {
    private Long ownerId;
    private String name;
    private String projectType;
}
