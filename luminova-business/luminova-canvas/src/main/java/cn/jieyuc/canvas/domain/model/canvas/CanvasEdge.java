package cn.jieyuc.canvas.domain.model.canvas;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Builder
public class CanvasEdge implements Serializable {

    private CanvasEdgeId id;
    private CanvasId canvasId;
    private String edgeKey;
    private CanvasNodeId sourceNodeId;
    private CanvasNodeId targetNodeId;
    private String label;
    @Builder.Default
    private Map<String, Object> config = new LinkedHashMap<>();
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static CanvasEdge connect(
            CanvasId canvasId,
            String edgeKey,
            CanvasNodeId sourceNodeId,
            CanvasNodeId targetNodeId
    ) {
        if (canvasId == null || sourceNodeId == null || targetNodeId == null) {
            throw new IllegalArgumentException("Canvas and edge node ids must not be null");
        }
        if (sourceNodeId.equals(targetNodeId)) {
            throw new IllegalArgumentException("A node cannot connect to itself");
        }
        if (edgeKey == null || edgeKey.isBlank()) {
            throw new IllegalArgumentException("Edge key must not be blank");
        }
        return CanvasEdge.builder()
                .canvasId(canvasId)
                .edgeKey(edgeKey.trim())
                .sourceNodeId(sourceNodeId)
                .targetNodeId(targetNodeId)
                .build();
    }
}
