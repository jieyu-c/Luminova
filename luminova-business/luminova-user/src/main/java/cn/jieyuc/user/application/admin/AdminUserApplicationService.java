package cn.jieyuc.user.application.admin;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.application.port.AuthenticatedUserProvider;
import cn.jieyuc.user.application.port.UserSessionRevoker;
import cn.jieyuc.user.application.audit.SecurityAuditApplicationService;
import cn.jieyuc.user.application.audit.SecurityAuditEvent;
import cn.jieyuc.user.domain.account.AccountStatus;
import cn.jieyuc.user.domain.account.PhoneNumber;
import cn.jieyuc.user.domain.account.Role;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserAccountRepository;
import cn.jieyuc.user.domain.account.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;

/**
 * 管理员用户管理应用服务。
 *
 * <p>每个用例先从账号聚合确认操作者角色，不信任客户端或请求参数中的角色。禁用账号后
 * 在事务提交成功时撤销目标用户全部会话。</p>
 */
@Service
public class AdminUserApplicationService {
    private final UserAccountRepository repository;
    private final AdminUserQueryRepository queryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserSessionRevoker sessionRevoker;
    private final SecurityAuditApplicationService auditService;

    public AdminUserApplicationService(UserAccountRepository repository,
                                       AdminUserQueryRepository queryRepository,
                                       AuthenticatedUserProvider authenticatedUserProvider,
                                       UserSessionRevoker sessionRevoker,
                                       SecurityAuditApplicationService auditService) {
        this.repository = repository;
        this.queryRepository = queryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.sessionRevoker = sessionRevoker;
        this.auditService = auditService;
    }

    /** 查询脱敏用户分页。 */
    @Transactional(readOnly = true)
    public AdminUserPage query(int page, int size, String status, Long userId, String phone) {
        requireAdmin();
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String normalized = phone == null || phone.isBlank() ? null : PhoneNumber.normalize(phone).value();
        return queryRepository.query(page, size, status, userId, normalized);
    }

    /** 启用或禁用目标用户。 */
    @Transactional
    public AdminUserSummary changeStatus(long targetUserId, AccountStatus targetStatus, String reason) {
        UserAccount operator = requireAdmin();
        if (reason == null || reason.trim().isEmpty() || reason.length() > 32) {
            throw new IllegalArgumentException("状态变更原因不能为空且不能超过 32 个字符");
        }
        UserAccount target = repository.findById(new UserId(targetUserId))
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        if (targetStatus == AccountStatus.DISABLED) {
            target.disable(operator.id(), OffsetDateTime.now());
        } else {
            target.enable(OffsetDateTime.now());
        }
        repository.save(target);
        auditService.record(new SecurityAuditEvent(
                targetStatus == AccountStatus.DISABLED ? "USER_DISABLED" : "USER_ENABLED",
                operator.id().value(), target.id().value(), null, null,
                "SUCCESS", reason.trim(), null, null, "user-admin"
        ));
        if (targetStatus == AccountStatus.DISABLED) {
            afterCommit(() -> sessionRevoker.revokeAll(targetUserId));
        }
        String masked = target.identities().isEmpty() ? null : target.identities().getFirst().maskedIdentifier();
        return new AdminUserSummary(target.id().value(), masked, target.nickname(),
                target.role().name(), target.status().name(), target.createdAt());
    }

    private UserAccount requireAdmin() {
        UserAccount operator = repository.findById(new UserId(authenticatedUserProvider.currentUserId()))
                .orElseThrow(() -> new BizException("FORBIDDEN", "无权访问"));
        if (operator.role() != Role.ADMIN || operator.status() != AccountStatus.ACTIVE) {
            throw new BizException("FORBIDDEN", "无权访问");
        }
        return operator;
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
