package cn.jieyuc.canvas.domain.model.canvas;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Builder
public class CanvasNode implements Serializable {

    private CanvasNodeId id;
    private CanvasId canvasId;
    private String nodeKey;
    private CanvasNodeType nodeType;
    private String title;
    private CanvasNodeStatus status;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal width;
    private BigDecimal height;
    private Integer zIndex;
    @Builder.Default
    private Map<String, Object> content = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> presentation = new LinkedHashMap<>();
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static CanvasNode create(
            CanvasId canvasId,
            String nodeKey,
            CanvasNodeType nodeType,
            BigDecimal positionX,
            BigDecimal positionY
    ) {
        if (canvasId == null || nodeType == null) {
            throw new IllegalArgumentException("Canvas id and node type must not be null");
        }
        if (nodeKey == null || nodeKey.isBlank()) {
            throw new IllegalArgumentException("Node key must not be blank");
        }
        if (positionX == null || positionY == null) {
            throw new IllegalArgumentException("Node position must not be null");
        }
        return CanvasNode.builder()
                .canvasId(canvasId)
                .nodeKey(nodeKey.trim())
                .nodeType(nodeType)
                .status(CanvasNodeStatus.DRAFT)
                .positionX(positionX)
                .positionY(positionY)
                .zIndex(0)
                .version(0L)
                .build();
    }

    public void moveTo(BigDecimal x, BigDecimal y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Node position must not be null");
        }
        this.positionX = x;
        this.positionY = y;
    }

    public void resize(BigDecimal width, BigDecimal height) {
        if (width == null || height == null
                || width.signum() < 0 || height.signum() < 0) {
            throw new IllegalArgumentException("Node size must not be negative");
        }
        this.width = width;
        this.height = height;
    }

    public void updateContent(Map<String, Object> content) {
        this.content = content == null ? new LinkedHashMap<>() : new LinkedHashMap<>(content);
    }

    public void changeStatus(CanvasNodeStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Node status must not be null");
        }
        this.status = status;
    }
}
