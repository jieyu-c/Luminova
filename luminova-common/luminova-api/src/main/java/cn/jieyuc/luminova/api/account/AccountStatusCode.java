package cn.jieyuc.luminova.api.account;

/**
 * 跨上下文传递的账号状态码。
 *
 * <p>认证快照仍用字符串携带状态，避免改动 Dubbo 载荷。取值必须与账号上下文
 * {@code AccountStatus} 的名称一致，比较和写入都走 {@link #code()}。</p>
 */
public enum AccountStatusCode {
    ACTIVE,
    DISABLED;

    /** 快照和会话里保存的状态码。 */
    public String code() {
        return name();
    }

    /** 快照中的状态是否就是当前枚举值。空值视为不匹配。 */
    public boolean matches(String code) {
        return name().equals(code);
    }
}
