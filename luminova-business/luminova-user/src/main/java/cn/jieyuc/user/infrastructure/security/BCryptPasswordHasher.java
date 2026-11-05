package cn.jieyuc.user.infrastructure.security;

import cn.jieyuc.user.domain.account.PasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt 密码哈希 Adapter。
 *
 * <p>默认 cost 为 12，可按生产机器压测结果配置。原始密码和哈希均不得记录日志。</p>
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasher(@Value("${luminova.security.password.bcrypt-strength:12}") int strength) {
        this.encoder = new BCryptPasswordEncoder(strength);
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
