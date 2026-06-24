package cn.jieyuc.canvas.domain.model.project;

public enum ProjectStatus {
    DRAFT((short) 0),
    IN_PROGRESS((short) 1),
    PENDING_REVIEW((short) 2),
    COMPLETED((short) 3);

    private final short code;

    ProjectStatus(short code) {
        this.code = code;
    }

    public short code() {
        return code;
    }

    public static ProjectStatus of(Short code) {
        if (code == null) {
            return null;
        }
        for (ProjectStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown project status: " + code);
    }
}
