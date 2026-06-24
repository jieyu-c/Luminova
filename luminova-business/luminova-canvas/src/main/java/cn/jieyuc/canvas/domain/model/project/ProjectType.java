package cn.jieyuc.canvas.domain.model.project;

public enum ProjectType {
    GENERAL,
    SHORT_DRAMA,
    COMIC,
    AD,
    TALKING_HEAD;

    public static ProjectType of(String value) {
        return value == null ? null : ProjectType.valueOf(value);
    }
}
