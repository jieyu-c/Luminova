package cn.jieyuc.user.application.audit;

import cn.jieyuc.luminova.api.account.SecurityAuditCommand;
import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import org.mapstruct.Mapper;

/**
 * 跨上下文审计命令到本上下文审计事件的转换。
 *
 * <p>两边字段一一对应。命令里的标识必须已经脱敏，映射过程不还原、不补全手机号。</p>
 */
@Mapper(config = DomainMappingConfig.class)
public interface SecurityAuditEventConverter {

    SecurityAuditEvent toEvent(SecurityAuditCommand command);
}
