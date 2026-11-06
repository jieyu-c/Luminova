package cn.jieyuc.user.application.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.api.account.AccountStatusCode;
import cn.jieyuc.luminova.api.account.SecurityAuditCommand;
import cn.jieyuc.luminova.api.account.SecurityAuditEventType;
import cn.jieyuc.luminova.api.account.SecurityAuditIdentityType;
import cn.jieyuc.luminova.api.account.SecurityAuditResult;
import cn.jieyuc.user.application.admin.AdminUserSummary;
import cn.jieyuc.user.application.admin.AdminUserSummaryConverter;
import cn.jieyuc.user.application.audit.SecurityAuditEvent;
import cn.jieyuc.user.application.audit.SecurityAuditEventConverter;
import cn.jieyuc.user.domain.account.AccountStatus;
import cn.jieyuc.user.domain.account.IdentityKey;
import cn.jieyuc.user.domain.account.IdentityType;
import cn.jieyuc.user.domain.account.LoginIdentity;
import cn.jieyuc.user.domain.account.PasswordCredential;
import cn.jieyuc.user.domain.account.Role;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserId;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * 用户主体到应用视图和跨上下文契约的转换。
 */
class AccountViewConverterTest {
    private final UserProfileViewConverter profileConverter = Mappers.getMapper(UserProfileViewConverter.class);
    private final AccountAuthSnapshotConverter snapshotConverter = Mappers.getMapper(AccountAuthSnapshotConverter.class);
    private final AdminUserSummaryConverter summaryConverter = Mappers.getMapper(AdminUserSummaryConverter.class);
    private final SecurityAuditEventConverter auditConverter = Mappers.getMapper(SecurityAuditEventConverter.class);

    @Test
    @DisplayName("资料、认证快照和管理员摘要只带走用例已经选定的脱敏标识")
    void shouldMapSelectedViews() {
        OffsetDateTime created = OffsetDateTime.parse("2026-11-05T01:00:00Z");
        LoginIdentity identity = new LoginIdentity(9,
                new IdentityKey(IdentityType.PHONE, IdentityKey.LOCAL_PROVIDER, "13800138000"),
                "138****8000", created, new PasswordCredential("hash-value", created));
        UserAccount account = new UserAccount(new UserId(8), "昵称", null, Role.USER, AccountStatus.ACTIVE,
                null, created, created, 0, List.of(identity));

        UserProfileView profile = profileConverter.toView(account, "138****8000");
        AccountAuthSnapshot snapshot = snapshotConverter.toSnapshot(account, identity);
        AdminUserSummary summary = summaryConverter.toSummary(account, null);

        assertEquals(8L, profile.id());
        assertEquals("138****8000", profile.maskedPhone());
        assertEquals(Role.USER.name(), profile.role());
        assertEquals(AccountStatus.ACTIVE.name(), profile.status());
        assertNull(profile.avatarUrl());
        assertEquals(8L, snapshot.userId());
        assertEquals(9L, snapshot.identityId());
        assertEquals(Role.USER.name(), snapshot.role());
        assertEquals(AccountStatusCode.ACTIVE.code(), snapshot.status());
        assertNull(summary.maskedPhone());
        assertEquals(8L, summary.userId());
        assertEquals(created, summary.createdAt());
    }

    @Test
    @DisplayName("审计命令按字段原样进入本上下文事件")
    void shouldCopyAuditCommand() {
        SecurityAuditCommand command = new SecurityAuditCommand(SecurityAuditEventType.LOGIN_FAILURE.code(), null, 8L,
                SecurityAuditIdentityType.PHONE.code(), "138****8000", SecurityAuditResult.FAILURE.code(),
                "BAD_PASSWORD", null, null, "request-1");

        SecurityAuditEvent event = auditConverter.toEvent(command);

        assertEquals(SecurityAuditEventType.LOGIN_FAILURE.code(), event.eventType());
        assertNull(event.actorUserId());
        assertEquals(8L, event.targetUserId());
        assertEquals("request-1", event.requestId());
    }
}
