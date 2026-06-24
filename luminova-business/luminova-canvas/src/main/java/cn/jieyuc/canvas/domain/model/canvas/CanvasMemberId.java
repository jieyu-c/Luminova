package cn.jieyuc.canvas.domain.model.canvas;

import java.io.Serializable;

public record CanvasMemberId(Long value) implements Serializable {

    public static CanvasMemberId of(Long value) {
        return value == null ? null : new CanvasMemberId(value);
    }
}
