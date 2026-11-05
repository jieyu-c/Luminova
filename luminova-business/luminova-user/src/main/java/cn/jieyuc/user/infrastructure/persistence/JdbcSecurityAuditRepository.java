package cn.jieyuc.user.infrastructure.persistence;

import cn.jieyuc.user.application.audit.SecurityAuditEvent;
import cn.jieyuc.user.application.audit.SecurityAuditRepository;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

/**
 * PostgreSQL 安全审计追加 Adapter。
 *
 * <p>审计表只允许追加，不提供业务更新方法。User-Agent 会截断为 300 字符；无效 IP
 * 以 null 保存，避免协议层伪造内容破坏审计写入。</p>
 */
@Repository
public class JdbcSecurityAuditRepository implements SecurityAuditRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcSecurityAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void append(SecurityAuditEvent event) {
        jdbcTemplate.update("""
                INSERT INTO security_audit_log
                (id, event_type, actor_user_id, target_user_id, identity_type,
                 identifier_masked, result, reason_code, ip, user_agent, request_id, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::inet, ?, ?, ?)
                """, IdWorker.getId(), event.eventType(), event.actorUserId(), event.targetUserId(),
                event.identityType(), event.identifierMasked(), event.result(), event.reasonCode(),
                validIp(event.ip()), truncate(event.userAgent(), 300), event.requestId(),
                Timestamp.from(OffsetDateTime.now().toInstant()));
    }

    private String validIp(String value) {
        if (value == null) {
            return null;
        }
        try {
            InetAddress.getByName(value);
            return value;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
