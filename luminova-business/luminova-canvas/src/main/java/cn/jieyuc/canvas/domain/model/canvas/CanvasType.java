package cn.jieyuc.canvas.domain.model.canvas;

public enum CanvasType {
    MAIN,
    STORYBOARD,
    CHARACTER_TEST,
    STYLE_TEST,
    ATMOSPHERE_TEST;

    public static CanvasType of(String value) {
        return value == null ? null : CanvasType.valueOf(value);
    }
}
