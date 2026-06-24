package cn.jieyuc.canvas.domain.model.canvas;

import java.io.Serializable;

public record CanvasEdgeId(Long value) implements Serializable {

    public static CanvasEdgeId of(Long value) {
        return value == null ? null : new CanvasEdgeId(value);
    }
}
