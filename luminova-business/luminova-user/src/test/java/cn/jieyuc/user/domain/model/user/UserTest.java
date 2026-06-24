package cn.jieyuc.user.domain.model.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldRejectUnavailableAccount() {
        User user = User.builder()
                .status(UserStatus.LOCKED)
                .build();

        assertThrows(IllegalStateException.class, user::ensureOperational);
    }

    @Test
    void shouldRejectSamePasswordWhenChangingPassword() {
        Password password = Password.of("Password1");
        User user = User.builder()
                .passwordHash(password.passwordHash())
                .status(UserStatus.NORMAL)
                .build();

        assertThrows(IllegalArgumentException.class, () -> user.changePassword(password));
    }
}
