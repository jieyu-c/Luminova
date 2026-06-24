package cn.jieyuc.canvas.domain.model.canvas;

public enum CanvasNodeType {
    SHOT,
    CHARACTER,
    SCENE,
    DIALOGUE,
    STYLE,
    SHOT_CONTEXT,
    ASSET,
    MUSIC,
    EFFECT,
    TEXT;

    public static CanvasNodeType of(String value) {
        return value == null ? null : CanvasNodeType.valueOf(value);
    }
}
