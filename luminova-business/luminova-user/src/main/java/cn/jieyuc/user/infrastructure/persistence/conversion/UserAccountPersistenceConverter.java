package cn.jieyuc.user.infrastructure.persistence.conversion;

import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import cn.jieyuc.user.domain.account.IdentityKey;
import cn.jieyuc.user.domain.account.IdentityType;
import cn.jieyuc.user.domain.account.LoginIdentity;
import cn.jieyuc.user.domain.account.PasswordCredential;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserId;
import cn.jieyuc.user.infrastructure.persistence.entity.LoginIdentityEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.PasswordCredentialEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.UserAccountEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 用户主体聚合与三张持久化表之间的结构转换。
 *
 * <p>装载必须走聚合和值对象的构造器，昵称、身份数量等不变量仍由领域类型校验。
 * {@link #toDomain} 只把仓储已经读出的三张表记录装成聚合，自己不查询数据库。
 * 转换器不决定账号能否登录，也不生成密码哈希。凭证行没有独立的领域创建时间，
 * 因此创建时间沿用身份验证时间，更新时间沿用最近一次密码变更时间。</p>
 */
@Mapper(config = DomainMappingConfig.class)
public interface UserAccountPersistenceConverter {

    UserAccountEntity toAccountEntity(UserAccount account);

    @Mapping(target = "id", source = "identity.id")
    @Mapping(target = "userId", source = "account.id")
    @Mapping(target = "identityType", source = "identity.key.type")
    @Mapping(target = "provider", source = "identity.key.provider")
    @Mapping(target = "identifier", source = "identity.key.identifier")
    @Mapping(target = "identifierMasked", source = "identity.maskedIdentifier")
    @Mapping(target = "verifiedAt", source = "identity.verifiedAt")
    @Mapping(target = "createdAt", source = "account.createdAt")
    @Mapping(target = "updatedAt", source = "account.updatedAt")
    LoginIdentityEntity toIdentityEntity(UserAccount account, LoginIdentity identity);

    @Mapping(target = "identityId", source = "id")
    @Mapping(target = "passwordHash", source = "passwordCredential.passwordHash")
    @Mapping(target = "passwordChangedAt", source = "passwordCredential.changedAt")
    @Mapping(target = "createdAt", source = "verifiedAt")
    @Mapping(target = "updatedAt", source = "passwordCredential.changedAt")
    PasswordCredentialEntity toCredentialEntity(LoginIdentity identity);

    UserAccount toAccount(UserAccountEntity account, List<LoginIdentity> identities);

    /**
     * 把用户主体、登录身份和密码凭证装成聚合。
     *
     * <p>凭证按身份主键对齐。某条身份没有凭证行时，聚合里的密码凭证保持为空，
     * 不在这里补默认哈希。</p>
     */
    default UserAccount toDomain(UserAccountEntity account, List<LoginIdentityEntity> identities,
                                 List<PasswordCredentialEntity> credentials) {
        Map<Long, PasswordCredentialEntity> credentialsByIdentity = new HashMap<>();
        for (PasswordCredentialEntity credential : credentials) {
            credentialsByIdentity.putIfAbsent(credential.getIdentityId(), credential);
        }
        List<LoginIdentity> loginIdentities = identities.stream()
                .map(identity -> toIdentity(identity, credentialsByIdentity.get(identity.getId())))
                .toList();
        return toAccount(account, loginIdentities);
    }

    @Mapping(target = "key", source = "entity")
    @Mapping(target = "maskedIdentifier", source = "entity.identifierMasked")
    @Mapping(target = "passwordCredential", source = "credential")
    LoginIdentity toIdentity(LoginIdentityEntity entity, PasswordCredentialEntity credential);

    @Mapping(target = "changedAt", source = "passwordChangedAt")
    PasswordCredential toCredential(PasswordCredentialEntity credential);

    default UserId toUserId(Long id) {
        if (id == null) {
            return null;
        }
        return new UserId(id);
    }

    default Long toLong(UserId id) {
        if (id == null) {
            return null;
        }
        return id.value();
    }

    /**
     * 三列身份键还原为值对象。提供方的大小写规范化由 {@link IdentityKey} 自己完成。
     */
    default IdentityKey toIdentityKey(LoginIdentityEntity entity) {
        if (entity == null) {
            return null;
        }
        return new IdentityKey(IdentityType.valueOf(entity.getIdentityType()),
                entity.getProvider(), entity.getIdentifier());
    }
}
