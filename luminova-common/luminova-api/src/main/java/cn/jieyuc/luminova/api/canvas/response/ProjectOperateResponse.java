package cn.jieyuc.luminova.api.canvas.response;

import cn.jieyuc.luminova.api.canvas.response.data.ProjectInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ProjectOperateResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private ProjectInfo projectInfo;
}
