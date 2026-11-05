package cn.jieyuc.user.application.audit;

/**
 * 账号上下文接收的结构化安全审计事件。
 *
 * <p>identifierMasked 必须已经脱敏，事件中禁止出现原始凭证和 Token。</p>
 */
public record SecurityAuditEvent(String eventType, Long actorUserId, Long targetUserId,
                                 String identityType, String identifierMasked, String result,
                                 String reasonCode, String ip, String userAgent, String requestId) {
}
