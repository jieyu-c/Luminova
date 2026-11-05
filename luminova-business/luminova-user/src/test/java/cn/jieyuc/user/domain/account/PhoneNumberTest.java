package cn.jieyuc.user.domain.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 手机号值对象领域测试。
 */
class PhoneNumberTest {

    @Test
    @DisplayName("大陆手机号应统一规范化为 E.164 格式并支持安全脱敏")
    void shouldNormalizeAndMaskPhone() {
        PhoneNumber phone = PhoneNumber.normalize("138 0013 8000");

        assertEquals("+8613800138000", phone.value());
        assertEquals("+86138****8000", phone.masked());
    }

    @Test
    @DisplayName("非法手机号必须在进入聚合前被拒绝")
    void shouldRejectInvalidPhone() {
        assertThrows(IllegalArgumentException.class, () -> PhoneNumber.normalize("12345"));
    }
}
