package cn.jieyuc.user.infrastructure.cache;

import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.domain.model.user.Username;
import cn.jieyuc.user.infrastructure.persistence.converter.UserPersistenceConverter;
import cn.jieyuc.user.infrastructure.persistence.entity.UserPO;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserMapper;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachedUserQuery {

    private static final int CACHE_EXPIRE_SECONDS = 3600;

    @Autowired
    private UserMapper userMapper;

    private final UserPersistenceConverter converter = UserPersistenceConverter.INSTANCE;

    @Cached(
            name = UserCacheNames.BY_ID,
            key = "#userId.value",
            expire = CACHE_EXPIRE_SECONDS,
            cacheType = CacheType.BOTH,
            cacheNullValue = true
    )
    public User findById(UserId userId) {
        UserPO userPO = userMapper.selectById(userId.value());
        return userPO == null ? null : converter.toDomain(userPO);
    }

    @Cached(
            name = UserCacheNames.BY_PHONE,
            key = "#phone.value",
            expire = CACHE_EXPIRE_SECONDS,
            cacheType = CacheType.BOTH,
            cacheNullValue = true
    )
    public User findByPhone(Phone phone) {
        UserPO userPO = userMapper.selectOne(
                Wrappers.<UserPO>lambdaQuery()
                        .eq(UserPO::getPhone, phone.value())
        );
        return userPO == null ? null : converter.toDomain(userPO);
    }

    @Cached(
            name = UserCacheNames.BY_USERNAME,
            key = "#username.value",
            expire = CACHE_EXPIRE_SECONDS,
            cacheType = CacheType.BOTH,
            cacheNullValue = true
    )
    public User findByUsername(Username username) {
        UserPO userPO = userMapper.selectOne(
                Wrappers.<UserPO>lambdaQuery()
                        .eq(UserPO::getUsername, username.value())
        );
        return userPO == null ? null : converter.toDomain(userPO);
    }

    @CacheInvalidate(name = UserCacheNames.BY_USERNAME, key = "#username")
    public void evictByUsername(String username) {
    }
}
