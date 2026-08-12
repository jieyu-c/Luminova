package cn.jieyuc.luminova.api.canvas.response.data;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class ProjectInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long projectId;
    private Long ownerId;
    private String name;
    private String projectType;
    private String description;
    private String coverUrl;
    private String aspectRatio;
    private Integer targetDurationMs;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
