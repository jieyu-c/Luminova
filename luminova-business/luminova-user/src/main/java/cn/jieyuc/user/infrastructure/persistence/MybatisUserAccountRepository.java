package cn.jieyuc.user.infrastructure.persistence;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.domain.account.AccountStatus;
import cn.jieyuc.user.domain.account.IdentityKey;
import cn.jieyuc.user.domain.account.IdentityType;
import cn.jieyuc.user.domain.account.LoginIdentity;
import cn.jieyuc.user.domain.account.PasswordCredential;
import cn.jieyuc.user.domain.account.Role;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserAccountRepository;
import cn.jieyuc.user.domain.account.UserId;
import cn.jieyuc.user.infrastructure.persistence.entity.LoginIdentityEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.PasswordCredentialEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.UserAccountEntity;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserAccountMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 基于 MyBatis 的用户主体聚合仓储 Adapter。
 *
 * <p>负责在领域聚合与三张持久化表之间转换。并发注册产生的身份唯一键冲突被映射为
 * 稳定业务错误；本类不决定密码规则或账号是否可以登录。</p>
 */
@Repository
public class MybatisUserAccountRepository implements UserAccountRepository {
    private final UserAccountMapper mapper;

    public MybatisUserAccountRepository(UserAccountMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<UserAccount> findById(UserId userId) {
        return Optional.ofNullable(mapper.findAccountById(userId.value())).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findByIdentity(IdentityKey identityKey) {
        UserAccountEntity entity = mapper.findAccountByIdentity(
                identityKey.type().name(), identityKey.provider(), identityKey.identifier());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public void save(UserAccount account) {
        UserAccountEntity existing = mapper.findAccountById(account.id().value());
        try {
            if (existing == null) {
                insertAggregate(account);
            } else {
                updateAggregate(account);
            }
        } catch (DuplicateKeyException exception) {
            throw new BizException("PHONE_ALREADY_REGISTERED", "手机号已注册");
        }
    }

    private void insertAggregate(UserAccount account) {
        mapper.insertAccount(toEntity(account));
        for (LoginIdentity identity : account.identities()) {
            mapper.insertIdentity(toEntity(account, identity));
            if (identity.passwordCredential() != null) {
                mapper.insertPasswordCredential(toEntity(identity));
            }
        }
    }

    private void updateAggregate(UserAccount account) {
        int updated = mapper.updateAccount(toEntity(account));
        if (updated != 1) {
            throw new BizException("CONCURRENT_MODIFICATION", "用户数据已被其他请求修改，请重试");
        }
        for (LoginIdentity identity : account.identities()) {
            if (identity.passwordCredential() != null) {
                mapper.updatePasswordCredential(toEntity(identity));
            }
        }
    }

    private UserAccount toDomain(UserAccountEntity account) {
        List<LoginIdentity> identities = mapper.findIdentities(account.getId()).stream()
                .map(identity -> {
                    PasswordCredentialEntity credential = mapper.findPasswordCredential(identity.getId());
                    PasswordCredential password = credential == null ? null
                            : new PasswordCredential(credential.getPasswordHash(), credential.getPasswordChangedAt());
                    return new LoginIdentity(identity.getId(),
                            new IdentityKey(IdentityType.valueOf(identity.getIdentityType()),
                                    identity.getProvider(), identity.getIdentifier()),
                            identity.getIdentifierMasked(), identity.getVerifiedAt(), password);
                })
                .toList();
        return new UserAccount(new UserId(account.getId()), account.getNickname(), account.getAvatarUrl(),
                Role.valueOf(account.getRole()), AccountStatus.valueOf(account.getStatus()),
                account.getLastLoginAt(), account.getCreatedAt(), account.getUpdatedAt(),
                account.getVersion(), identities);
    }

    private UserAccountEntity toEntity(UserAccount account) {
        UserAccountEntity entity = new UserAccountEntity();
        entity.setId(account.id().value());
        entity.setNickname(account.nickname());
        entity.setAvatarUrl(account.avatarUrl());
        entity.setRole(account.role().name());
        entity.setStatus(account.status().name());
        entity.setLastLoginAt(account.lastLoginAt());
        entity.setCreatedAt(account.createdAt());
        entity.setUpdatedAt(account.updatedAt());
        entity.setVersion(account.version());
        return entity;
    }

    private LoginIdentityEntity toEntity(UserAccount account, LoginIdentity identity) {
        LoginIdentityEntity entity = new LoginIdentityEntity();
        entity.setId(identity.id());
        entity.setUserId(account.id().value());
        entity.setIdentityType(identity.key().type().name());
        entity.setProvider(identity.key().provider());
        entity.setIdentifier(identity.key().identifier());
        entity.setIdentifierMasked(identity.maskedIdentifier());
        entity.setVerifiedAt(identity.verifiedAt());
        entity.setCreatedAt(account.createdAt());
        entity.setUpdatedAt(account.updatedAt());
        return entity;
    }

    private PasswordCredentialEntity toEntity(LoginIdentity identity) {
        PasswordCredentialEntity entity = new PasswordCredentialEntity();
        entity.setIdentityId(identity.id());
        entity.setPasswordHash(identity.passwordCredential().passwordHash());
        entity.setPasswordChangedAt(identity.passwordCredential().changedAt());
        entity.setCreatedAt(identity.verifiedAt());
        entity.setUpdatedAt(identity.passwordCredential().changedAt());
        return entity;
    }
}
