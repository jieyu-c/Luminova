package cn.jieyuc.user.application.account;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.api.account.PasswordAuthenticationResult;
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
    private final String dummyHash;

    public AccountCredentialApplicationService(UserAccountRepository repository, PasswordHasher passwordHasher) {
        this(repository, passwordHasher, new PasswordPolicy(), Clock.systemUTC());
    }

    AccountCredentialApplicationService(UserAccountRepository repository, PasswordHasher passwordHasher,
                                        PasswordPolicy passwordPolicy, Clock clock) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.clock = clock;
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
        IdentityKey key = new IdentityKey(IdentityType.PHONE, "LOCAL", phone.value());
        UserAccount account = repository.findByIdentity(key).orElse(null);
        if (account == null) {
            passwordHasher.matches(rawPassword == null ? "" : rawPassword, dummyHash);
            return PasswordAuthenticationResult.failure();
        }
        LoginIdentity identity = account.requireIdentity(key);
        PasswordCredential credential = identity.passwordCredential();
        boolean matched = credential != null
                && passwordHasher.matches(rawPassword == null ? "" : rawPassword, credential.passwordHash());
        if (!matched || account.status() != AccountStatus.ACTIVE) {
            return PasswordAuthenticationResult.failure();
        }
        account.recordSuccessfulLogin(OffsetDateTime.now(clock));
        repository.save(account);
        return PasswordAuthenticationResult.success(snapshot(account, identity));
    }

    /**
     * 使用已完成手机号所有权验证的命令重置密码。
     *
     * <p>本地事务只更新凭证；调用方应在事务成功后撤销全部会话。</p>
     */
    @Transactional
    public AccountAuthSnapshot resetPhonePassword(String rawPhone, String newPassword) {
        passwordPolicy.validate(newPassword);
        PhoneNumber phone = PhoneNumber.normalize(rawPhone);
        IdentityKey key = new IdentityKey(IdentityType.PHONE, "LOCAL", phone.value());
        UserAccount account = repository.findByIdentity(key)
                .orElseThrow(() -> new BizException("VERIFICATION_CODE_INVALID", "验证码无效或已过期"));
        LoginIdentity identity = account.requireIdentity(key);
        if (identity.passwordCredential() != null
                && passwordHasher.matches(newPassword, identity.passwordCredential().passwordHash())) {
            throw new BizException("PASSWORD_REUSED", "新密码不能与当前密码相同");
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        identity.replacePassword(new PasswordCredential(passwordHasher.hash(newPassword), now));
        repository.save(account);
        return snapshot(account, identity);
    }

    /**
     * 查询会话校验所需的最小账号快照。
     */
    @Transactional(readOnly = true)
    public AccountAuthSnapshot getAuthSnapshot(long userId) {
        UserAccount account = repository.findById(new UserId(userId))
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        LoginIdentity identity = account.identities().getFirst();
        return snapshot(account, identity);
    }

    private AccountAuthSnapshot snapshot(UserAccount account, LoginIdentity identity) {
        return new AccountAuthSnapshot(account.id().value(), identity.id(),
                account.role().name(), account.status().name());
    }
}
