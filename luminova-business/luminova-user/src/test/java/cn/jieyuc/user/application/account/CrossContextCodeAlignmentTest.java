package cn.jieyuc.user.application.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.jieyuc.luminova.api.account.AccountStatusCode;
import cn.jieyuc.luminova.api.account.SecurityAuditIdentityType;
import cn.jieyuc.user.domain.account.AccountStatus;
import cn.jieyuc.user.domain.account.IdentityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 账号上下文枚举与跨上下文契约码必须同名，否则快照和审计会写成另一套字符串。
 */
class CrossContextCodeAlignmentTest {

    @Test
    @DisplayName("账号状态和登录身份类型与跨上下文契约一一对应")
    void shouldKeepCrossContextCodesAligned() {
        assertEquals(AccountStatus.values().length, AccountStatusCode.values().length);
        for (AccountStatus status : AccountStatus.values()) {
            assertEquals(status.name(), AccountStatusCode.valueOf(status.name()).code());
        }
        assertEquals(IdentityType.values().length, SecurityAuditIdentityType.values().length);
        for (IdentityType type : IdentityType.values()) {
            assertEquals(type.name(), SecurityAuditIdentityType.valueOf(type.name()).code());
        }
    }
}
