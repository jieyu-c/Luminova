package cn.jieyuc.canvas.domain.model.canvas;

public enum CanvasNodeStatus {
    DRAFT((short) 0),
    READY((short) 1),
    RUNNING((short) 2),
    SUCCEEDED((short) 3),
    FAILED((short) 4);

    private final short code;

    CanvasNodeStatus(short code) {
        this.code = code;
    }

    public short code() {
        return code;
    }

    public static CanvasNodeStatus of(Short code) {
        if (code == null) {
            return null;
        }
        for (CanvasNodeStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown canvas node status: " + code);
    }
}
