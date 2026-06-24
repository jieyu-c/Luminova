package cn.jieyuc.user.domain.model.user;

public record Nickname(String value) {

    private static final int MAX_LENGTH = 64;

    public static Nickname of(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname must not be blank");
        }
        String normalized = nickname.trim();
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Nickname is too long");
        }
        return new Nickname(normalized);
    }

    public static Nickname ofUnchecked(String nickname) {
        return new Nickname(nickname);
    }
}
