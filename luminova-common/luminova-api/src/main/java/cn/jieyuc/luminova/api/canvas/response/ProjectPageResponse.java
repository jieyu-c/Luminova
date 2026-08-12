package cn.jieyuc.luminova.api.canvas.response;

import cn.jieyuc.luminova.api.canvas.response.data.ProjectInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectPageResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pageNo;
    private Long pageSize;
    private Long total;
    private Long pages;
    private List<ProjectInfo> records = new ArrayList<>();
}
