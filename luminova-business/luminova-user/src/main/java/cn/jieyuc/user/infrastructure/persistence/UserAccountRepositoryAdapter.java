package cn.jieyuc.user.infrastructure.persistence;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.domain.account.IdentityKey;
import cn.jieyuc.user.domain.account.LoginIdentity;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserAccountRepository;
import cn.jieyuc.user.domain.account.UserId;
import cn.jieyuc.user.infrastructure.persistence.entity.LoginIdentityEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.PasswordCredentialEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.UserAccountEntity;
import cn.jieyuc.user.infrastructure.persistence.mapper.PasswordCredentialMapper;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserAccountMapper;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserIdentityMapper;
import cn.jieyuc.user.infrastructure.persistence.conversion.UserAccountPersistenceConverter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/**
 * 用户主体聚合的持久化仓储。
 *
 * <p>单表读写走 {@code BaseMapper} 和 {@code LambdaQueryWrapper}。身份查找先命中唯一键，
 * 再按用户 ID 装载聚合，不在 Mapper 中写连接 SQL。读出的行交给
 * {@link UserAccountPersistenceConverter#toDomain} 装成聚合，仓储不复制字段，也不绕过聚合构造器。
 * 乐观锁由实体上的 {@code @Version} 交给插件处理；影响行数不是 1 表示版本冲突。
 * 本类不决定密码规则或账号是否可以登录。</p>
 */
@Repository
public class UserAccountRepositoryAdapter implements UserAccountRepository {
    private final UserAccountMapper accountMapper;
    private final UserIdentityMapper identityMapper;
    private final PasswordCredentialMapper credentialMapper;
    private final UserAccountPersistenceConverter accountConverter;

    public UserAccountRepositoryAdapter(UserAccountMapper accountMapper,
                                        UserIdentityMapper identityMapper,
                                        PasswordCredentialMapper credentialMapper,
                                        UserAccountPersistenceConverter accountConverter) {
        this.accountMapper = accountMapper;
        this.identityMapper = identityMapper;
        this.credentialMapper = credentialMapper;
        this.accountConverter = accountConverter;
    }

    @Override
    public Optional<UserAccount> findById(UserId userId) {
        return Optional.ofNullable(accountMapper.selectById(userId.value())).map(this::loadAggregate);
    }

    @Override
    public Optional<UserAccount> findByIdentity(IdentityKey identityKey) {
        LoginIdentityEntity identity = identityMapper.selectOne(Wrappers.<LoginIdentityEntity>lambdaQuery()
                .eq(LoginIdentityEntity::getIdentityType, identityKey.type().name())
                .eq(LoginIdentityEntity::getProvider, identityKey.provider())
                .eq(LoginIdentityEntity::getIdentifier, identityKey.identifier()));
        if (identity == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountMapper.selectById(identity.getUserId())).map(this::loadAggregate);
    }

    /** 插入或按版本更新聚合。身份唯一键冲突和乐观锁冲突分别映射为业务错误。 */
    @Override
    public void save(UserAccount account) {
        UserAccountEntity existing = accountMapper.selectById(account.id().value());
        try {
            if (existing == null) {
                insertAggregate(account);
            } else {
                updateAggregate(account);
            }
        } catch (DuplicateKeyException exception) {
            // 登录身份唯一键冲突统一表现为手机号已注册，不把数据库约束名返回给调用方。
            throw new BizException(BizErrorCode.PHONE_ALREADY_REGISTERED);
        }
    }

    private void insertAggregate(UserAccount account) {
        accountMapper.insert(accountConverter.toAccountEntity(account));
        for (LoginIdentity identity : account.identities()) {
            identityMapper.insert(accountConverter.toIdentityEntity(account, identity));
            if (identity.passwordCredential() != null) {
                credentialMapper.insert(accountConverter.toCredentialEntity(identity));
            }
        }
    }

    private void updateAggregate(UserAccount account) {
        int updated = accountMapper.updateById(accountConverter.toAccountEntity(account));
        if (updated != 1) {
            // 影响行数不是 1 表示版本已被其他请求写过，不能用本次快照覆盖。
            throw new BizException(BizErrorCode.CONCURRENT_MODIFICATION);
        }
        for (LoginIdentity identity : account.identities()) {
            if (identity.passwordCredential() != null) {
                credentialMapper.updateById(accountConverter.toCredentialEntity(identity));
            }
        }
    }

    /**
     * 读取一个用户主体的身份和凭证行。行到聚合的组装在转换器中完成。
     */
    private UserAccount loadAggregate(UserAccountEntity account) {
        List<LoginIdentityEntity> identities = identityMapper.selectList(Wrappers.<LoginIdentityEntity>lambdaQuery()
                .eq(LoginIdentityEntity::getUserId, account.getId())
                .orderByAsc(LoginIdentityEntity::getId));
        List<PasswordCredentialEntity> credentials = new ArrayList<>();
        for (LoginIdentityEntity identity : identities) {
            PasswordCredentialEntity credential = credentialMapper.selectById(identity.getId());
            if (credential != null) {
                credentials.add(credential);
            }
        }
        return accountConverter.toDomain(account, identities, credentials);
    }
}
