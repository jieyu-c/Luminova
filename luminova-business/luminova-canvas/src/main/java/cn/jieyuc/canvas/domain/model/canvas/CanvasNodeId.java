package cn.jieyuc.canvas.domain.model.canvas;

import java.io.Serializable;

public record CanvasNodeId(Long value) implements Serializable {

    public static CanvasNodeId of(Long value) {
        return value == null ? null : new CanvasNodeId(value);
    }
}
