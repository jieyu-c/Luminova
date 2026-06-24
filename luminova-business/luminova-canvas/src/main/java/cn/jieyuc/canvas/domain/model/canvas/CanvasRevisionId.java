package cn.jieyuc.canvas.domain.model.canvas;

import java.io.Serializable;

public record CanvasRevisionId(Long value) implements Serializable {

    public static CanvasRevisionId of(Long value) {
        return value == null ? null : new CanvasRevisionId(value);
    }
}
