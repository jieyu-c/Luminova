package cn.jieyuc.luminovaauth.infrastructure.ratelimit;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.port.login.LoginRateLimiter;
import cn.jieyuc.luminovaauth.application.login.LoginType;
import cn.jieyuc.luminovaauth.infrastructure.security.IdentifierDigester;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 登录限流 Adapter。
 *
 * <p>IP 维度限制每分钟 20 次尝试，身份连续失败 5 次后锁定 15 分钟。Redis key 只保存
 * 标识摘要，不保存完整手机号。Redis 不可用时抛出异常，绝不绕过认证保护。</p>
 */
@Component
public class RedisLoginRateLimiter implements LoginRateLimiter {
    private static final long MAX_IP_ATTEMPTS = 20;
    private static final long MAX_IDENTITY_FAILURES = 5;
    private static final Duration IP_WINDOW = Duration.ofMinutes(1);
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(15);

    private final StringRedisTemplate redisTemplate;
    private final IdentifierDigester digester;

    public RedisLoginRateLimiter(StringRedisTemplate redisTemplate, IdentifierDigester digester) {
        this.redisTemplate = redisTemplate;
        this.digester = digester;
    }

    /**
     * 先限制来源 IP 的尝试次数，再拒绝已被连续失败锁定的身份。
     *
     * <p>两处对外都是限流，文案由不同错误码常量区分。</p>
     */
    @Override
    public void checkAttempt(LoginType loginType, String identifier, String clientIp) {
        String ipKey = "auth:login:ip:" + digester.digest(clientIp == null ? "unknown" : clientIp);
        if (incrementWithTtl(ipKey, IP_WINDOW) > MAX_IP_ATTEMPTS) {
            throw new BizException(BizErrorCode.RATE_LIMITED);
        }
        String failure = redisTemplate.opsForValue().get(failureKey(loginType, identifier));
        if (failure != null && Long.parseLong(failure) >= MAX_IDENTITY_FAILURES) {
            throw new BizException(BizErrorCode.LOGIN_ATTEMPT_RATE_LIMITED);
        }
    }

    @Override
    public void onSuccess(LoginType loginType, String identifier) {
        redisTemplate.delete(failureKey(loginType, identifier));
    }

    @Override
    public void onFailure(LoginType loginType, String identifier) {
        incrementWithTtl(failureKey(loginType, identifier), FAILURE_WINDOW);
    }

    /**
     * 计数并在键首次出现时设置过期时间。后续递增不再刷新过期，否则窗口会被不断往后推。
     */
    private long incrementWithTtl(String key, Duration ttl) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1) {
            redisTemplate.expire(key, ttl);
        }
        return value == null ? 0 : value;
    }

    private String failureKey(LoginType loginType, String identifier) {
        return "auth:login:fail:" + loginType.name() + ":" + digester.digest(identifier);
    }
}
