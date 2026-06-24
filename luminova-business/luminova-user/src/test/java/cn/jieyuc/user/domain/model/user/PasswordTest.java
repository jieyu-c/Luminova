package cn.jieyuc.user.domain.model.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    @Test
    void shouldHashWithBcryptAndMatchRawPassword() {
        Password password = Password.of("Password1");

        assertTrue(password.verify());
        assertTrue(password.passwordHash().startsWith("$2"));
        assertTrue(Password.matches("Password1", password.passwordHash()));
        assertFalse(Password.matches("Password2", password.passwordHash()));
    }

    @Test
    void shouldRejectInvalidFormat() {
        Password password = Password.of("short");

        assertFalse(password.verify());
    }
}
