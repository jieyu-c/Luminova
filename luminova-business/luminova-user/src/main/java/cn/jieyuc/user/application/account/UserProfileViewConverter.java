package cn.jieyuc.user.application.account;

import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import cn.jieyuc.user.domain.account.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 用户主体到本人资料视图的转换。
 *
 * <p>脱敏手机号由用例先从身份集合中选出。转换器不决定哪一个身份对外展示。</p>
 */
@Mapper(config = DomainMappingConfig.class)
public interface UserProfileViewConverter {

    @Mapping(target = "id", source = "account.id.value")
    @Mapping(target = "maskedPhone", source = "maskedPhone")
    UserProfileView toView(UserAccount account, String maskedPhone);
}
