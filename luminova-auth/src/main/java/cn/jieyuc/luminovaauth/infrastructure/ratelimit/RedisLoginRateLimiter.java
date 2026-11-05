package cn.jieyuc.luminovaauth.infrastructure.ratelimit;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.login.LoginRateLimiter;
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

    @Override
    public void checkAttempt(LoginType loginType, String identifier, String clientIp) {
        String ipKey = "auth:login:ip:" + digester.digest(clientIp == null ? "unknown" : clientIp);
        if (incrementWithTtl(ipKey, IP_WINDOW) > MAX_IP_ATTEMPTS) {
            throw new BizException("RATE_LIMITED", "请求过于频繁，请稍后重试");
        }
        String failure = redisTemplate.opsForValue().get(failureKey(loginType, identifier));
        if (failure != null && Long.parseLong(failure) >= MAX_IDENTITY_FAILURES) {
            throw new BizException("RATE_LIMITED", "登录尝试过多，请稍后重试");
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
