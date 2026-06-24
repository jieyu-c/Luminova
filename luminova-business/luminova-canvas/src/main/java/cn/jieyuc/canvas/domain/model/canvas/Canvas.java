package cn.jieyuc.canvas.domain.model.canvas;

import cn.jieyuc.canvas.domain.model.project.EpisodeId;
import cn.jieyuc.canvas.domain.model.project.ProjectId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class Canvas implements Serializable {

    private static final BigDecimal MIN_ZOOM = new BigDecimal("0.1");
    private static final BigDecimal MAX_ZOOM = new BigDecimal("5");

    private CanvasId id;
    private ProjectId projectId;
    private EpisodeId episodeId;
    private Long ownerId;
    private String name;
    private CanvasType canvasType;
    private CanvasStatus status;
    private String coverUrl;
    private BigDecimal viewportX;
    private BigDecimal viewportY;
    private BigDecimal viewportZoom;
    private Long version;
    private Long lastEditedBy;
    private OffsetDateTime lastEditedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static Canvas create(Long ownerId, ProjectId projectId, String name, CanvasType canvasType) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Canvas owner must not be null");
        }
        return Canvas.builder()
                .ownerId(ownerId)
                .projectId(projectId)
                .name(requireName(name))
                .canvasType(canvasType == null ? CanvasType.MAIN : canvasType)
                .status(CanvasStatus.DRAFT)
                .viewportX(BigDecimal.ZERO)
                .viewportY(BigDecimal.ZERO)
                .viewportZoom(BigDecimal.ONE)
                .version(0L)
                .build();
    }

    public void rename(String name) {
        this.name = requireName(name);
    }

    public void updateViewport(BigDecimal x, BigDecimal y, BigDecimal zoom, Long editorId) {
        if (x == null || y == null || zoom == null) {
            throw new IllegalArgumentException("Canvas viewport values must not be null");
        }
        if (zoom.compareTo(MIN_ZOOM) < 0 || zoom.compareTo(MAX_ZOOM) > 0) {
            throw new IllegalArgumentException("Canvas zoom must be between 0.1 and 5");
        }
        this.viewportX = x;
        this.viewportY = y;
        this.viewportZoom = zoom;
        markEditedBy(editorId);
    }

    public void markEditedBy(Long editorId) {
        this.lastEditedBy = editorId;
        this.lastEditedAt = OffsetDateTime.now();
    }

    public void changeStatus(CanvasStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Canvas status must not be null");
        }
        this.status = status;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Canvas name must not be blank");
        }
        return name.trim();
    }
}
