package cn.jieyuc.luminova.api.account;

/**
 * 安全审计的操作结果。
 *
 * <p>只区分成功和失败。具体失败原因使用业务错误码，不把原因写进结果字段。</p>
 */
public enum SecurityAuditResult {
    SUCCESS,
    FAILURE;

    /** 写入审计命令和审计行的结果码。 */
    public String code() {
        return name();
    }
}
