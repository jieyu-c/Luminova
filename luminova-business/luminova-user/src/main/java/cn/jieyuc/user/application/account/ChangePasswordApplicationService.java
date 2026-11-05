package cn.jieyuc.user.application.account;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.application.port.AuthenticatedUserProvider;
import cn.jieyuc.user.application.port.UserSessionRevoker;
import cn.jieyuc.user.domain.account.LoginIdentity;
import cn.jieyuc.user.domain.account.PasswordCredential;
import cn.jieyuc.user.domain.account.PasswordHasher;
import cn.jieyuc.user.domain.account.PasswordPolicy;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserAccountRepository;
import cn.jieyuc.user.domain.account.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;

/**
 * 登录后修改密码应用服务。
 *
 * <p>在本地事务中验证旧密码并保存新哈希，事务提交后撤销全部会话。撤销动作不能提前，
 * 否则数据库回滚时用户仍会被错误地下线。</p>
 */
@Service
public class ChangePasswordApplicationService {
    private final UserAccountRepository repository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserSessionRevoker sessionRevoker;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    public ChangePasswordApplicationService(UserAccountRepository repository,
                                            AuthenticatedUserProvider authenticatedUserProvider,
                                            UserSessionRevoker sessionRevoker,
                                            PasswordHasher passwordHasher) {
        this.repository = repository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.sessionRevoker = sessionRevoker;
        this.passwordHasher = passwordHasher;
    }

    /** 修改当前用户密码并在提交后撤销全部会话。 */
    @Transactional
    public void change(String currentPassword, String newPassword) {
        passwordPolicy.validate(newPassword);
        long userId = authenticatedUserProvider.currentUserId();
        UserAccount account = repository.findById(new UserId(userId))
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        LoginIdentity identity = account.identities().stream()
                .filter(item -> item.passwordCredential() != null)
                .findFirst()
                .orElseThrow(() -> new BizException("PASSWORD_LOGIN_UNAVAILABLE", "当前账号未启用密码登录"));
        if (!passwordHasher.matches(currentPassword, identity.passwordCredential().passwordHash())) {
            throw new BizException("INVALID_CURRENT_PASSWORD", "当前密码错误");
        }
        if (passwordHasher.matches(newPassword, identity.passwordCredential().passwordHash())) {
            throw new BizException("PASSWORD_REUSED", "新密码不能与当前密码相同");
        }
        OffsetDateTime now = OffsetDateTime.now();
        identity.replacePassword(new PasswordCredential(passwordHasher.hash(newPassword), now));
        repository.save(account);
        afterCommit(() -> sessionRevoker.revokeAll(userId));
    }

    private void afterCommit(Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
