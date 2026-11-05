package cn.jieyuc.user.domain.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 用户主体聚合不变量测试。
 */
class UserAccountTest {

    @Test
    @DisplayName("新注册用户应同时拥有普通角色、可用状态和已验证手机号身份")
    void shouldCreateCompleteRegisteredAccount() {
        OffsetDateTime now = OffsetDateTime.parse("2026-11-05T00:00:00Z");
        UserAccount account = UserAccount.register(new UserId(1), 2,
                PhoneNumber.normalize("13800138000"), new PasswordCredential("hash", now), now);

        assertEquals(Role.USER, account.role());
        assertEquals(AccountStatus.ACTIVE, account.status());
        assertEquals(1, account.identities().size());
    }

    @Test
    @DisplayName("管理员不能禁用自己的用户主体")
    void shouldRejectDisablingSelf() {
        OffsetDateTime now = OffsetDateTime.parse("2026-11-05T00:00:00Z");
        UserAccount account = UserAccount.register(new UserId(1), 2,
                PhoneNumber.normalize("13800138000"), new PasswordCredential("hash", now), now);

        assertThrows(IllegalArgumentException.class,
                () -> account.disable(new UserId(1), now.plusMinutes(1)));
    }
}
