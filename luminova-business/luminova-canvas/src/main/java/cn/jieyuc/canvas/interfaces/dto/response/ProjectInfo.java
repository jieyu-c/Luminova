package cn.jieyuc.canvas.interfaces.dto.response;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProjectInfo {
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
