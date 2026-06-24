package cn.jieyuc.user.domain.model.user;

import java.util.regex.Pattern;

public record AvatarUrl(String value) {

    private static final int MAX_LENGTH = 512;
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);

    public static AvatarUrl of(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new IllegalArgumentException("Avatar url must not be blank");
        }
        String normalized = avatarUrl.trim();
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Avatar url is too long");
        }
        if (!URL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Avatar url must be a valid http or https url");
        }
        return new AvatarUrl(normalized);
    }
}
