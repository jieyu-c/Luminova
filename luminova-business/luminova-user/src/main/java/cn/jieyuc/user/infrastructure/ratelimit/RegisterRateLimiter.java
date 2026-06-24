package cn.jieyuc.user.infrastructure.ratelimit;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.infrastructure.web.UserExceptionEnums;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegisterRateLimiter {

    private static final String KEY_PREFIX = "user:register:rate:";
    private static final long PERMITS = 5;
    private static final long INTERVAL = 1;
    private static final RateIntervalUnit INTERVAL_UNIT = RateIntervalUnit.HOURS;

    @Autowired
    private RedissonClient redissonClient;

    public void check(Phone phone) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(KEY_PREFIX + phone.value());
        rateLimiter.trySetRate(RateType.OVERALL, PERMITS, INTERVAL, INTERVAL_UNIT);
        if (!rateLimiter.tryAcquire()) {
            throw new BizException(
                    UserExceptionEnums.USER_REGISTER_RATE_LIMIT.name(),
                    "Register too frequently, please try again later"
            );
        }
    }
}
