package cn.jieyuc.generation.domain.model.task;

public enum GenerationTaskStatus {
    PENDING((short) 0),
    RUNNING((short) 1),
    SUCCEEDED((short) 2),
    FAILED((short) 3),
    CANCELLED((short) 4);

    private final short code;

    GenerationTaskStatus(short code) {
        this.code = code;
    }

    public short code() {
        return code;
    }

    public static GenerationTaskStatus of(Short code) {
        if (code == null) {
            return null;
        }
        for (GenerationTaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown generation task status: " + code);
    }
}
