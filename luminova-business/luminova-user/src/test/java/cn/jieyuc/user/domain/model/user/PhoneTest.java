package cn.jieyuc.user.domain.model.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneTest {

    @Test
    void shouldRejectNullPhone() {
        assertThrows(IllegalArgumentException.class, () -> Phone.of(null));
    }

    @Test
    void shouldRejectBlankPhone() {
        assertThrows(IllegalArgumentException.class, () -> Phone.of("  "));
    }

    @Test
    void shouldAcceptValidPhone() {
        Phone phone = Phone.of("13800138000");

        assertEquals("13800138000", phone.value());
    }
}
