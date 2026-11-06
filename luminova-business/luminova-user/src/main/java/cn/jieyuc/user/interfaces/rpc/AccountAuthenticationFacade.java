package cn.jieyuc.user.interfaces.rpc;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.api.account.AccountAuthenticationApi;
import cn.jieyuc.luminova.api.account.PasswordAuthenticationResult;
import cn.jieyuc.luminova.api.account.PhonePasswordQuery;
import cn.jieyuc.luminova.api.account.RegisterPhoneUserCommand;
import cn.jieyuc.luminova.api.account.ResetPhonePasswordCommand;
import cn.jieyuc.luminova.api.account.SecurityAuditCommand;
import cn.jieyuc.user.application.account.AccountAuthSnapshotConverter;
import cn.jieyuc.user.application.account.AccountCredentialApplicationService;
import cn.jieyuc.user.application.account.RegisterUserApplicationService;
import cn.jieyuc.user.application.audit.SecurityAuditApplicationService;
import cn.jieyuc.user.application.audit.SecurityAuditEventConverter;
import cn.jieyuc.user.domain.account.UserAccount;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 账号上下文 Dubbo 防腐层。
 *
 * <p>只负责把跨上下文 DTO 转成应用层调用并映射稳定结果，不直接访问 Mapper，
 * 也不向认证上下文返回领域对象或密码哈希。</p>
 */
@DubboService
public class AccountAuthenticationFacade implements AccountAuthenticationApi {
    private final RegisterUserApplicationService registerService;
    private final AccountCredentialApplicationService credentialService;
    private final SecurityAuditApplicationService auditService;
    private final AccountAuthSnapshotConverter snapshotConverter;
    private final SecurityAuditEventConverter auditEventConverter;

    public AccountAuthenticationFacade(RegisterUserApplicationService registerService,
                                       AccountCredentialApplicationService credentialService,
                                       SecurityAuditApplicationService auditService,
                                       AccountAuthSnapshotConverter snapshotConverter,
                                       SecurityAuditEventConverter auditEventConverter) {
        this.registerService = registerService;
        this.credentialService = credentialService;
        this.auditService = auditService;
        this.snapshotConverter = snapshotConverter;
        this.auditEventConverter = auditEventConverter;
    }

    @Override
    public AccountAuthSnapshot registerPhoneUser(RegisterPhoneUserCommand command) {
        UserAccount account = registerService.registerPhone(command.phone(), command.password());
        return snapshotConverter.toSnapshot(account, account.identities().getFirst());
    }

    @Override
    public PasswordAuthenticationResult authenticatePhonePassword(PhonePasswordQuery query) {
        return credentialService.authenticatePhonePassword(query.phone(), query.password());
    }

    @Override
    public AccountAuthSnapshot resetPhonePassword(ResetPhonePasswordCommand command) {
        return credentialService.resetPhonePassword(command.phone(), command.newPassword());
    }

    @Override
    public AccountAuthSnapshot getAuthSnapshot(long userId) {
        return credentialService.getAuthSnapshot(userId);
    }

    @Override
    public void recordSecurityAudit(SecurityAuditCommand command) {
        auditService.record(auditEventConverter.toEvent(command));
    }
}
