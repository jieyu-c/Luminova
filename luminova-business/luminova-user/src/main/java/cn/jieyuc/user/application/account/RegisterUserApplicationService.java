package cn.jieyuc.user.application.account;

import cn.jieyuc.user.application.port.UserIdentifierGenerator;
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
 * 注册用户应用服务。
 *
 * <p>负责在一个本地事务中创建用户主体、已验证手机号身份和密码凭证。本类不验证短信、
 * 不签发会话，也不把密码哈希返回给调用方。</p>
 */
@Service
public class RegisterUserApplicationService {
    private final UserAccountRepository repository;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final UserIdentifierGenerator identifierGenerator;
    private final Clock clock;

    public RegisterUserApplicationService(UserAccountRepository repository,
                                          PasswordHasher passwordHasher,
                                          UserIdentifierGenerator identifierGenerator) {
        this(repository, passwordHasher, new PasswordPolicy(), identifierGenerator, Clock.systemUTC());
    }

    RegisterUserApplicationService(UserAccountRepository repository, PasswordHasher passwordHasher,
                                   PasswordPolicy passwordPolicy, UserIdentifierGenerator identifierGenerator,
                                   Clock clock) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.identifierGenerator = identifierGenerator;
        this.clock = clock;
    }

    /**
     * 注册手机号用户。
     *
     * <p>唯一性最终由数据库身份唯一约束保证，避免“先查询再插入”的并发竞态。</p>
     *
     * @param rawPhone    用户输入手机号
     * @param rawPassword 原始密码，禁止记录日志
     * @return 新用户主体
     */
    @Transactional
    public UserAccount registerPhone(String rawPhone, String rawPassword) {
        PhoneNumber phone = PhoneNumber.normalize(rawPhone);
        passwordPolicy.validate(rawPassword);
        OffsetDateTime now = OffsetDateTime.now(clock);
        PasswordCredential credential = new PasswordCredential(passwordHasher.hash(rawPassword), now);
        UserAccount account = UserAccount.register(
                new UserId(identifierGenerator.nextId()),
                identifierGenerator.nextId(),
                phone,
                credential,
                now
        );
        repository.save(account);
        return account;
    }
}
