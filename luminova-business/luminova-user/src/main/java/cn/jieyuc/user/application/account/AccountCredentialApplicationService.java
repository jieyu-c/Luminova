package cn.jieyuc.user.application.account;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.api.account.PasswordAuthenticationResult;
import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.domain.account.AccountStatus;
import cn.jieyuc.user.domain.account.IdentityKey;
import cn.jieyuc.user.domain.account.IdentityType;
import cn.jieyuc.user.domain.account.LoginIdentity;
import cn.jieyuc.user.domain.account.PasswordCredential;
import cn.jieyuc.user.domain.account.PasswordHasher;
import cn.jieyuc.user.domain.account.PasswordPolicy;
import cn.jieyuc.user.domain.account.PhoneNumber;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserAccountRepository;
import cn.jieyuc.user.domain.account.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * 账号凭证应用服务。
 *
 * <p>负责加载用户聚合并验证或替换本地密码。不存在的身份也执行一次假哈希校验，
 * 使调用方难以通过耗时差异枚举账号。本类不签发或撤销登录会话。</p>
 */
@Service
public class AccountCredentialApplicationService {
    private final UserAccountRepository repository;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final Clock clock;
    private final AccountAuthSnapshotConverter snapshotConverter;
    private final String dummyHash;

    /**
     * 容器只装配此构造器。密码策略和时钟在这里固定，避免选中测试构造器。
     */
    @Autowired
    public AccountCredentialApplicationService(UserAccountRepository repository, PasswordHasher passwordHasher,
                                               AccountAuthSnapshotConverter snapshotConverter) {
        this(repository, passwordHasher, new PasswordPolicy(), Clock.systemUTC(), snapshotConverter);
    }

    AccountCredentialApplicationService(UserAccountRepository repository, PasswordHasher passwordHasher,
                                        PasswordPolicy passwordPolicy, Clock clock,
                                        AccountAuthSnapshotConverter snapshotConverter) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.clock = clock;
        this.snapshotConverter = snapshotConverter;
        this.dummyHash = passwordHasher.hash("Luminova-Dummy-Password-2026");
    }

    /**
     * 认证手机号密码。
     *
     * @param rawPhone    用户输入手机号
     * @param rawPassword 原始密码，禁止记录日志
     * @return 不泄漏失败原因的认证结果
     */
    @Transactional
    public PasswordAuthenticationResult authenticatePhonePassword(String rawPhone, String rawPassword) {
        PhoneNumber phone = PhoneNumber.normalize(rawPhone);
        IdentityKey key = new IdentityKey(IdentityType.PHONE, IdentityKey.LOCAL_PROVIDER, phone.value());
        UserAccount account = repository.findByIdentity(key).orElse(null);
        if (account == null) {
            // 不存在的身份也做一次哈希比较，避免用耗时差枚举账号。
            passwordHasher.matches(rawPassword == null ? "" : rawPassword, dummyHash);
            return PasswordAuthenticationResult.failure();
        }
        LoginIdentity identity = account.requireIdentity(key);
        PasswordCredential credential = identity.passwordCredential();
        boolean matched = credential != null
                && passwordHasher.matches(rawPassword == null ? "" : rawPassword, credential.passwordHash());
        // 密码错误和账号停用都返回失败，调用方无法从结果区分原因。
        if (!matched || account.status() != AccountStatus.ACTIVE) {
            return PasswordAuthenticationResult.failure();
        }
        account.recordSuccessfulLogin(OffsetDateTime.now(clock));
        repository.save(account);
        return PasswordAuthenticationResult.success(snapshotConverter.toSnapshot(account, identity));
    }

    /**
     * 使用已完成手机号所有权验证的命令重置密码。
     *
     * <p>本地事务只更新凭证；调用方应在事务成功后撤销全部会话。未注册手机号也返回
     * 验证码无效，避免这个接口暴露账号是否存在。</p>
     */
    @Transactional
    public AccountAuthSnapshot resetPhonePassword(String rawPhone, String newPassword) {
        passwordPolicy.validate(newPassword);
        PhoneNumber phone = PhoneNumber.normalize(rawPhone);
        IdentityKey key = new IdentityKey(IdentityType.PHONE, IdentityKey.LOCAL_PROVIDER, phone.value());
        // 发码不检查是否已注册。这里若返回用户不存在，调用方就能确认该手机号没有账号。
        UserAccount account = repository.findByIdentity(key)
                .orElseThrow(() -> new BizException(BizErrorCode.VERIFICATION_CODE_INVALID));
        LoginIdentity identity = account.requireIdentity(key);
        if (identity.passwordCredential() != null
                && passwordHasher.matches(newPassword, identity.passwordCredential().passwordHash())) {
            throw new BizException(BizErrorCode.PASSWORD_REUSED);
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        identity.replacePassword(new PasswordCredential(passwordHasher.hash(newPassword), now));
        repository.save(account);
        return snapshotConverter.toSnapshot(account, identity);
    }

    /**
     * 查询会话校验所需的最小账号快照。
     */
    @Transactional(readOnly = true)
    public AccountAuthSnapshot getAuthSnapshot(long userId) {
        UserAccount account = repository.findById(new UserId(userId))
                .orElseThrow(() -> new BizException(BizErrorCode.USER_NOT_FOUND));
        LoginIdentity identity = account.identities().getFirst();
        return snapshotConverter.toSnapshot(account, identity);
    }
}
