package cn.jieyuc.canvas.domain.model.canvas;

public enum CanvasStatus {
    DRAFT((short) 0),
    IN_PROGRESS((short) 1),
    PENDING_CONFIRMATION((short) 2),
    PENDING_REVIEW((short) 3),
    COMPLETED((short) 4);

    private final short code;

    CanvasStatus(short code) {
        this.code = code;
    }

    public short code() {
        return code;
    }

    public static CanvasStatus of(Short code) {
        if (code == null) {
            return null;
        }
        for (CanvasStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown canvas status: " + code);
    }
}
