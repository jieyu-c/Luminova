package cn.jieyuc.user.application.audit;

/**
 * 安全审计持久化端口。
 */
public interface SecurityAuditRepository {

    /** 保存一条脱敏安全审计事件。 */
    void append(SecurityAuditEvent event);
}
