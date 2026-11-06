package cn.jieyuc.user.infrastructure.persistence.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cn.jieyuc.luminova.api.account.SecurityAuditEventType;
import cn.jieyuc.luminova.api.account.SecurityAuditIdentityType;
import cn.jieyuc.luminova.api.account.SecurityAuditResult;
import cn.jieyuc.user.application.audit.SecurityAuditEvent;
import cn.jieyuc.user.infrastructure.persistence.entity.SecurityAuditLogEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * 审计事件写入行之前的清洗规则。
 */
class SecurityAuditPersistenceConverterTest {
    private final SecurityAuditPersistenceConverter converter = Mappers.getMapper(SecurityAuditPersistenceConverter.class);

    @Test
    @DisplayName("无效 IP 写成空，超长 User-Agent 截断，主键和写入时间留给仓储")
    void shouldSanitizeProtocolFields() {
        SecurityAuditEvent event = new SecurityAuditEvent(SecurityAuditEventType.LOGIN_FAILURE.code(), null, null,
                SecurityAuditIdentityType.PHONE.code(), "138****8000", SecurityAuditResult.FAILURE.code(),
                "BAD_PASSWORD", "not-an-ip", "a".repeat(301), "request-1");

        SecurityAuditLogEntity entity = converter.toEntity(event);

        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getIp());
        assertEquals(300, entity.getUserAgent().length());
        assertEquals(SecurityAuditEventType.LOGIN_FAILURE.code(), entity.getEventType());
        assertEquals("request-1", entity.getRequestId());
    }

    @Test
    @DisplayName("合法 IP 原样保留")
    void shouldKeepValidIp() {
        SecurityAuditEvent event = new SecurityAuditEvent(SecurityAuditEventType.LOGIN_SUCCESS.code(), 1L, 1L,
                SecurityAuditIdentityType.PHONE.code(), "138****8000", SecurityAuditResult.SUCCESS.code(),
                null, "127.0.0.1", null, "request-2");

        assertEquals("127.0.0.1", converter.toEntity(event).getIp());
        assertNull(converter.toEntity(event).getUserAgent());
    }
}
