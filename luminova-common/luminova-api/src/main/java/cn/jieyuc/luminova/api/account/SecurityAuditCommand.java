package cn.jieyuc.luminova.api.account;

import java.io.Serial;
import java.io.Serializable;

/**
 * 跨上下文安全审计命令。
 *
 * <p>只允许传递脱敏标识和机器可读原因，禁止携带密码、验证码、Token 或完整手机号。</p>
 */
public record SecurityAuditCommand(String eventType, Long actorUserId, Long targetUserId,
                                   String identityType, String identifierMasked, String result,
                                   String reasonCode, String ip, String userAgent, String requestId)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
