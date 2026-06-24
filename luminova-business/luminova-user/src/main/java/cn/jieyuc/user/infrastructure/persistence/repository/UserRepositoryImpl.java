package cn.jieyuc.user.infrastructure.persistence.repository;

import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.domain.model.user.Username;
import cn.jieyuc.user.domain.repository.UserRepository;
import cn.jieyuc.user.infrastructure.cache.CachedUserQuery;
import cn.jieyuc.user.infrastructure.cache.UserCacheNames;
import cn.jieyuc.user.infrastructure.persistence.converter.UserPersistenceConverter;
import cn.jieyuc.user.infrastructure.persistence.entity.UserPO;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserMapper;
import com.alicp.jetcache.anno.CacheInvalidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CachedUserQuery cachedUserQuery;

    private final UserPersistenceConverter converter = UserPersistenceConverter.INSTANCE;

    @Override
    public Optional<User> findById(UserId userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cachedUserQuery.findById(userId));
    }

    @Override
    public Optional<User> findByPhone(Phone phone) {
        if (phone == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cachedUserQuery.findByPhone(phone));
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cachedUserQuery.findByUsername(username));
    }

    @Override
    @CacheInvalidate(name = UserCacheNames.BY_ID, key = "#result.id.value")
    @CacheInvalidate(name = UserCacheNames.BY_PHONE, key = "#result.phone.value")
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        String previousUsername = null;
        if (user.getId() != null) {
            UserPO existingUser = userMapper.selectById(user.getId().value());
            if (existingUser != null) {
                previousUsername = existingUser.getUsername();
            }
        }
        UserPO userPO = converter.toPO(user);
        if (userPO.getId() == null) {
            userMapper.insert(userPO);
        } else {
            userMapper.updateById(userPO);
        }
        User savedUser = converter.toDomain(userPO);
        cachedUserQuery.evictByUsername(savedUser.getUsername());
        if (previousUsername != null && !previousUsername.equals(savedUser.getUsername())) {
            cachedUserQuery.evictByUsername(previousUsername);
        }
        return savedUser;
    }
}
