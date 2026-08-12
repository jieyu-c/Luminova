package cn.jieyuc.canvas.interfaces.dto.response;

import cn.jieyuc.luminova.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectPageResponse extends BaseResponse {
    private Long pageNo;
    private Long pageSize;
    private Long total;
    private Long pages;
    private List<ProjectInfo> records = new ArrayList<>();

    public static ProjectPageResponse success(Long pageNo, Long pageSize, Long total, Long pages, List<ProjectInfo> records) {
        ProjectPageResponse response = new ProjectPageResponse();
        response.setSuccess(true);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotal(total);
        response.setPages(pages);
        response.setRecords(records);
        return response;
    }
}
