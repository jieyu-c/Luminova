package cn.jieyuc.user.application.account;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.application.port.session.AuthenticatedUserProvider;
import cn.jieyuc.user.application.port.session.UserSessionRevoker;
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

    /**
     * 修改当前用户密码，并在事务提交后撤销全部会话。
     *
     * <p>先校验旧密码，再拒绝与当前哈希相同的新密码。会话撤销放在提交之后，
     * 避免事务回滚后用户仍被下线。</p>
     */
    @Transactional
    public void change(String currentPassword, String newPassword) {
        passwordPolicy.validate(newPassword);
        long userId = authenticatedUserProvider.currentUserId();
        UserAccount account = repository.findById(new UserId(userId))
                .orElseThrow(() -> new BizException(BizErrorCode.USER_NOT_FOUND));
        LoginIdentity identity = account.identities().stream()
                .filter(item -> item.passwordCredential() != null)
                .findFirst()
                .orElseThrow(() -> new BizException(BizErrorCode.PASSWORD_LOGIN_UNAVAILABLE));
        if (!passwordHasher.matches(currentPassword, identity.passwordCredential().passwordHash())) {
            throw new BizException(BizErrorCode.INVALID_CURRENT_PASSWORD);
        }
        if (passwordHasher.matches(newPassword, identity.passwordCredential().passwordHash())) {
            throw new BizException(BizErrorCode.PASSWORD_REUSED);
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
