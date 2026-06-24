package cn.jieyuc.user.domain.model.actionlog;

public enum UserActionType {
    REGISTER,
    LOGIN,
    LOGOUT,
    PASSWORD_CHANGE,
    PASSWORD_RESET,
    PHONE_BIND,
    EMAIL_BIND,
    PROFILE_UPDATE,
    ACCOUNT_LOCK,
    ACCOUNT_UNLOCK,
    ACCOUNT_CANCEL;

    public static UserActionType of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return UserActionType.valueOf(value);
    }
}
