package cn.jieyuc.luminovaauth.infrastructure.verification;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.port.verification.VerificationCodeRateLimiter;
import cn.jieyuc.luminovaauth.infrastructure.security.IdentifierDigester;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 验证码发送限流 Adapter。
 *
 * <p>同一手机号一分钟最多一次、一小时最多五次、一天最多十次；同一 IP 一小时最多
 * 二十次。所有手机号和 IP 在 Redis key 中均使用 HMAC 摘要。</p>
 */
@Component
public class RedisVerificationCodeRateLimiter implements VerificationCodeRateLimiter {
    private final StringRedisTemplate redisTemplate;
    private final IdentifierDigester digester;

    public RedisVerificationCodeRateLimiter(StringRedisTemplate redisTemplate, IdentifierDigester digester) {
        this.redisTemplate = redisTemplate;
        this.digester = digester;
    }

    /**
     * 按手机号的分钟、小时、天窗口和 IP 的小时窗口依次计数。任一窗口超限即拒绝。
     */
    @Override
    public void checkAndRecord(String normalizedPhone, String clientIp) {
        String phone = digester.digest(normalizedPhone);
        String ip = digester.digest(clientIp == null ? "unknown" : clientIp);
        check("auth:otp:phone:minute:" + phone, Duration.ofMinutes(1), 1);
        check("auth:otp:phone:hour:" + phone, Duration.ofHours(1), 5);
        check("auth:otp:phone:day:" + phone, Duration.ofDays(1), 10);
        check("auth:otp:ip:hour:" + ip, Duration.ofHours(1), 20);
    }

    /** 计数。只在键第一次出现时设置窗口，避免后续递增把过期时间不断延后。 */
    private void check(String key, Duration window, long limit) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1) {
            redisTemplate.expire(key, window);
        }
        if (value != null && value > limit) {
            throw new BizException(BizErrorCode.VERIFICATION_CODE_RATE_LIMITED);
        }
    }
}
