package cn.jieyuc.user.infrastructure.persistence.conversion;

import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import cn.jieyuc.user.application.audit.SecurityAuditEvent;
import cn.jieyuc.user.infrastructure.persistence.entity.SecurityAuditLogEntity;
import java.net.InetAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 安全审计事件到追加记录的转换。
 *
 * <p>主键和写入时间由仓储分配，不属于事件本身。无效 IP 写成 null，User-Agent 截断到 300，
 * 避免协议层伪造内容破坏 inet 列或撑开审计行。这里不保存原始凭证。</p>
 */
@Mapper(config = DomainMappingConfig.class)
public interface SecurityAuditPersistenceConverter {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "ip", source = "ip", qualifiedByName = "validIp")
    @Mapping(target = "userAgent", source = "userAgent", qualifiedByName = "userAgent")
    SecurityAuditLogEntity toEntity(SecurityAuditEvent event);

    @Named("validIp")
    default String validIp(String value) {
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

    @Named("userAgent")
    default String userAgent(String value) {
        if (value == null || value.length() <= 300) {
            return value;
        }
        return value.substring(0, 300);
    }
}
