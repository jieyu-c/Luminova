package cn.jieyuc.canvas.domain.model.canvas;

public enum CanvasNodeType {
    TEXT, IMAGE, VIDEO, AUDIO;

    public static CanvasNodeType of(String value) {
        return value == null ? null : CanvasNodeType.valueOf(value);
    }
}
