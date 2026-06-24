package cn.jieyuc.canvas.domain.model.canvas;

public enum CanvasMemberRole {
    OWNER,
    EDITOR,
    VIEWER;

    public static CanvasMemberRole of(String value) {
        return value == null ? null : CanvasMemberRole.valueOf(value);
    }
}
