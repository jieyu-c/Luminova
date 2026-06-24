package cn.jieyuc.user.infrastructure.username;

import cn.jieyuc.user.domain.model.user.Username;
import cn.jieyuc.user.domain.service.UsernameGenerator;
import cn.jieyuc.user.domain.service.UsernameUniquenessChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class DefaultUsernameGenerator implements UsernameGenerator {

    private static final int TEMP_USERNAME_RANDOM_LENGTH = 10;
    private static final int MAX_GENERATION_ATTEMPTS = 20;
    private static final char[] TEMP_USERNAME_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UsernameUniquenessChecker usernameUniquenessChecker;

    @Override
    public Username generateTemporary() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            Username candidate = Username.ofUnchecked("u" + randomSuffix());
            if (usernameUniquenessChecker.isAvailable(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate a temporary username");
    }

    private String randomSuffix() {
        char[] suffix = new char[TEMP_USERNAME_RANDOM_LENGTH];
        for (int i = 0; i < TEMP_USERNAME_RANDOM_LENGTH; i++) {
            suffix[i] = TEMP_USERNAME_ALPHABET[secureRandom.nextInt(TEMP_USERNAME_ALPHABET.length)];
        }
        return new String(suffix);
    }
}
