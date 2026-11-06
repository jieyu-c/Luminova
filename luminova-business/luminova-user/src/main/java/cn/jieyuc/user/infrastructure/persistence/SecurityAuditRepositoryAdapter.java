package cn.jieyuc.user.infrastructure.persistence;

import cn.jieyuc.user.application.audit.SecurityAuditEvent;
import cn.jieyuc.user.application.port.audit.SecurityAuditRepository;
import cn.jieyuc.user.infrastructure.persistence.entity.SecurityAuditLogEntity;
import cn.jieyuc.user.infrastructure.persistence.mapper.SecurityAuditLogMapper;
import cn.jieyuc.user.infrastructure.persistence.conversion.SecurityAuditPersistenceConverter;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Repository;

/**
 * 安全审计追加仓储。
 *
 * <p>审计表只允许追加，仓储不提供更新或删除。字段转换和 IP、User-Agent 清洗交给
 * {@link SecurityAuditPersistenceConverter}。主键和写入时间不属于事件内容，仍由仓储分配。</p>
 */
@Repository
public class SecurityAuditRepositoryAdapter implements SecurityAuditRepository {
    private final SecurityAuditLogMapper auditLogMapper;
    private final SecurityAuditPersistenceConverter auditConverter;

    public SecurityAuditRepositoryAdapter(SecurityAuditLogMapper auditLogMapper,
                                          SecurityAuditPersistenceConverter auditConverter) {
        this.auditLogMapper = auditLogMapper;
        this.auditConverter = auditConverter;
    }

    @Override
    public void append(SecurityAuditEvent event) {
        SecurityAuditLogEntity entity = auditConverter.toEntity(event);
        entity.setId(IdWorker.getId());
        entity.setCreatedAt(OffsetDateTime.now());
        auditLogMapper.insert(entity);
    }
}
