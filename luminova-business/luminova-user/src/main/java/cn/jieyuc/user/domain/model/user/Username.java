package cn.jieyuc.user.domain.model.user;

import java.util.regex.Pattern;

public record Username(String value) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{3,31}$");

    public static Username of(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        String normalized = username.trim();
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Username is invalid");
        }
        return new Username(normalized);
    }

    public static Username ofUnchecked(String username) {
        return new Username(username);
    }
}
