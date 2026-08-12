package cn.jieyuc.luminova.api.canvas.request;

import cn.jieyuc.luminova.base.request.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectPageRequest extends BaseRequest {
    private Long ownerId;
    private String status;
    private Long pageNo = 1L;
    private Long pageSize = 20L;
}
