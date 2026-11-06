package cn.jieyuc.luminova.api.account;

/**
 * 安全审计中记录的登录身份类型。
 *
 * <p>取值必须与账号上下文 {@code IdentityType} 的名称一致。审计只保存 {@link #code()}，
 * 不保存身份原值。</p>
 */
public enum SecurityAuditIdentityType {
    PHONE,
    EMAIL,
    WECHAT,
    OIDC;

    /** 写入审计命令和审计行的身份类型。 */
    public String code() {
        return name();
    }
}
