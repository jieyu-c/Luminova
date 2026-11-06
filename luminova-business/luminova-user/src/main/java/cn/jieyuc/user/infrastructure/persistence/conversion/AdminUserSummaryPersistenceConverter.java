package cn.jieyuc.user.infrastructure.persistence.conversion;

import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import cn.jieyuc.user.application.admin.AdminUserSummary;
import cn.jieyuc.user.infrastructure.persistence.entity.UserAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 管理员分页行到脱敏摘要的转换。
 *
 * <p>手机号在仓储里先按身份选出后再传入。转换器只搬运已经脱敏的值，不读取完整登录标识。</p>
 */
@Mapper(config = DomainMappingConfig.class)
public interface AdminUserSummaryPersistenceConverter {

    @Mapping(target = "userId", source = "account.id")
    @Mapping(target = "maskedPhone", source = "maskedPhone")
    AdminUserSummary toSummary(UserAccountEntity account, String maskedPhone);
}
