package cn.jieyuc.user.infrastructure.username;

import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.domain.model.user.Username;
import cn.jieyuc.user.domain.repository.UserRepository;
import cn.jieyuc.user.domain.service.UsernameUniquenessChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UsernameUniquenessCheckerImpl implements UsernameUniquenessChecker {

    private static final Logger LOG = LoggerFactory.getLogger(UsernameUniquenessCheckerImpl.class);

    @Autowired
    private UsernameBloomFilterSupport usernameBloomFilterSupport;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isAvailable(Username username) {
        return isAvailable(username, null);
    }

    @Override
    public boolean isAvailable(Username username, UserId excludeUserId) {
        if (username == null) {
            return false;
        }
        return userRepository.findByUsername(username)
                .map(existing -> excludeUserId != null && excludeUserId.equals(existing.getId()))
                .orElse(true);
    }

    @Override
    public void markUsed(Username username) {
        if (username == null) {
            return;
        }
        String value = username.value();
        CompletableFuture.runAsync(() -> usernameBloomFilterSupport.add(value))
                .exceptionally(ex -> {
                    LOG.warn("Failed to mark username in bloom filter: {}", value, ex);
                    return null;
                });
    }
}
