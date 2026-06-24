package cn.jieyuc.canvas.domain.model.canvas;

public enum CanvasEdgeType {
    DEFAULT,
    REFERENCE,
    SEQUENCE,
    CONSTRAINT;

    public static CanvasEdgeType of(String value) {
        return value == null ? null : CanvasEdgeType.valueOf(value);
    }
}
