package cn.jieyuc.user.domain.model.user;

public enum UserStatus {
    DISABLED((short) 0),
    NORMAL((short) 1),
    LOCKED((short) 2),
    CANCELLED((short) 3);

    private final short code;

    UserStatus(short code) {
        this.code = code;
    }

    public short code() {
        return code;
    }

    public static UserStatus of(Short code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status: " + code);
    }
}
