package cn.jieyuc.user.application.account;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import cn.jieyuc.user.domain.account.LoginIdentity;
import cn.jieyuc.user.domain.account.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 用户主体到跨上下文认证快照的转换。
 *
 * <p>快照放在应用层，是因为接口层只能依赖应用结果，不能让应用层反过来依赖接口。
 * 结果只含稳定 ID、角色和状态，不带走登录标识原值或密码哈希。</p>
 */
@Mapper(config = DomainMappingConfig.class)
public interface AccountAuthSnapshotConverter {

    @Mapping(target = "userId", source = "account.id.value")
    @Mapping(target = "identityId", source = "identity.id")
    @Mapping(target = "role", source = "account.role")
    @Mapping(target = "status", source = "account.status")
    AccountAuthSnapshot toSnapshot(UserAccount account, LoginIdentity identity);
}
