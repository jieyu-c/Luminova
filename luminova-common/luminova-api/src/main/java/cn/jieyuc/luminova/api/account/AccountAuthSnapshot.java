package cn.jieyuc.luminova.api.account;

import java.io.Serial;
import java.io.Serializable;

/**
 * 账号上下文向外提供的最小认证快照。
 *
 * <p>快照不包含登录身份原值和任何凭证，只用于统一账号状态检查与会话签发。</p>
 */
public record AccountAuthSnapshot(long userId, long identityId, String role, String status)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
