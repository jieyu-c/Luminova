package cn.jieyuc.user.application.admin;

import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import cn.jieyuc.user.domain.account.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 用户主体到管理员摘要的转换。
 *
 * <p>脱敏手机号由用例选择。状态变更后的摘要必须反映聚合里的新状态，而不是请求参数。</p>
 */
@Mapper(config = DomainMappingConfig.class)
public interface AdminUserSummaryConverter {

    @Mapping(target = "userId", source = "account.id.value")
    @Mapping(target = "maskedPhone", source = "maskedPhone")
    AdminUserSummary toSummary(UserAccount account, String maskedPhone);
}
