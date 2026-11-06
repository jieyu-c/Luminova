package cn.jieyuc.user.application.audit;

import cn.jieyuc.user.application.port.audit.SecurityAuditRepository;

import org.springframework.stereotype.Service;

/**
 * 安全审计应用服务。
 *
 * <p>负责检查跨上下文事件没有明显敏感原值并持久化。本类不决定业务操作是否成功。</p>
 */
@Service
public class SecurityAuditApplicationService {
    private final SecurityAuditRepository repository;

    public SecurityAuditApplicationService(SecurityAuditRepository repository) {
        this.repository = repository;
    }

    /** 保存一条安全审计事件。 */
    public void record(SecurityAuditEvent event) {
        if (event.eventType() == null || event.eventType().isBlank()
                || event.result() == null || event.requestId() == null) {
            throw new IllegalArgumentException("安全审计事件缺少必要字段");
        }
        repository.append(event);
    }
}
