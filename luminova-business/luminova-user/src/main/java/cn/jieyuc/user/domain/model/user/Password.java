package cn.jieyuc.user.domain.model.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public record Password(String password, String passwordHash) {

    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d.@$!%*?&]{8,20}$";

    public static Password of(String password) {
        return new Password(password, BCRYPT_ENCODER.encode(password));
    }

    public boolean verify() {
        return password != null && password.matches(PASSWORD_REGEX);
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        return BCRYPT_ENCODER.matches(rawPassword, storedHash);
    }
}
