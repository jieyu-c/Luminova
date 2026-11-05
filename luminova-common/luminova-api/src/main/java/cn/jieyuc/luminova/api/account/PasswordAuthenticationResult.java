package cn.jieyuc.luminova.api.account;

import java.io.Serial;
import java.io.Serializable;

/**
 * 密码认证结果。
 *
 * <p>失败结果不携带内部原因，防止跨上下文泄漏账号是否存在或被禁用。</p>
 */
public record PasswordAuthenticationResult(boolean authenticated, AccountAuthSnapshot snapshot)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建认证成功结果。
     *
     * @param snapshot 用户认证快照
     * @return 成功结果
     */
    public static PasswordAuthenticationResult success(AccountAuthSnapshot snapshot) {
        return new PasswordAuthenticationResult(true, snapshot);
    }

    /**
     * 创建不泄漏内部原因的认证失败结果。
     *
     * @return 失败结果
     */
    public static PasswordAuthenticationResult failure() {
        return new PasswordAuthenticationResult(false, null);
    }
}
