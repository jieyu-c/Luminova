package cn.jieyuc.user.domain.model.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvatarUrlTest {

    @Test
    void shouldAcceptHttpUrl() {
        AvatarUrl avatarUrl = AvatarUrl.of("https://cdn.example.com/a.png");

        assertEquals("https://cdn.example.com/a.png", avatarUrl.value());
    }

    @Test
    void shouldRejectNonHttpScheme() {
        assertThrows(IllegalArgumentException.class, () -> AvatarUrl.of("javascript:alert(1)"));
    }

    @Test
    void shouldRejectBlankUrl() {
        assertThrows(IllegalArgumentException.class, () -> AvatarUrl.of(" "));
    }
}
