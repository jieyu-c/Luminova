package cn.jieyuc.luminova.api.account;

/**
 * 安全审计事件类型。
 *
 * <p>持久化和跨上下文命令只保存 {@link #code()}，必须与已有审计行中的字符串一致。</p>
 */
public enum SecurityAuditEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    USER_DISABLED,
    USER_ENABLED;

    /** 写入审计命令和审计行的事件类型。 */
    public String code() {
        return name();
    }
}
