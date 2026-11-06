package cn.jieyuc.luminova.base.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 锁定对外错误码、默认文案和 HTTP 状态，避免重命名枚举常量时改掉接口契约。
 */
class BizErrorCodeTest {

    @Test
    @DisplayName("异常对外码使用枚举中的稳定码，限流细分常量仍对外表现为 RATE_LIMITED")
    void shouldExposeStableCodeInsteadOfEnumName() {
        BizException invalidCredentials = new BizException(BizErrorCode.INVALID_CREDENTIALS);
        BizException loginLocked = new BizException(BizErrorCode.LOGIN_ATTEMPT_RATE_LIMITED);

        assertEquals("INVALID_CREDENTIALS", invalidCredentials.getCode());
        assertEquals("手机号或密码错误", invalidCredentials.getMessage());
        assertEquals(401, invalidCredentials.getErrorCode().httpStatus());
        assertEquals("RATE_LIMITED", loginLocked.getCode());
        assertEquals("登录尝试过多，请稍后重试", loginLocked.getMessage());
        assertEquals(BizErrorCode.RATE_LIMITED.code(), BizErrorCode.VERIFICATION_CODE_RATE_LIMITED.code());
        assertEquals(429, BizErrorCode.VERIFICATION_CODE_RATE_LIMITED.httpStatus());
    }
}
