package cn.jieyuc.canvas.domain.model.canvas;

import java.io.Serializable;

public record CanvasId(Long value) implements Serializable {

    public static CanvasId of(Long value) {
        return value == null ? null : new CanvasId(value);
    }
}
