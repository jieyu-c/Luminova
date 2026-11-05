package cn.jieyuc.luminovaauth.infrastructure.verification;

import cn.jieyuc.luminovaauth.domain.verification.VerificationChallenge;
import cn.jieyuc.luminovaauth.domain.verification.VerificationChallengeRepository;
import cn.jieyuc.luminovaauth.domain.verification.VerificationPurpose;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Redis 验证挑战仓储 Adapter。
 *
 * <p>Lua 脚本在 Redis 内原子完成用途、手机号摘要、验证码摘要比较，失败计数以及成功删除。
 * 这避免并发请求同时消费同一验证码。Redis 中不保存明文手机号或验证码。</p>
 */
@Repository
public class RedisVerificationChallengeRepository implements VerificationChallengeRepository {
    private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>("""
            local values = redis.call('HMGET', KEYS[1], 'phoneDigest', 'codeDigest', 'purpose', 'failures', 'maxFailures')
            if not values[1] then return -1 end
            if values[1] ~= ARGV[1] or values[3] ~= ARGV[3] then return 0 end
            if values[2] == ARGV[2] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            local failures = redis.call('HINCRBY', KEYS[1], 'failures', 1)
            if failures >= tonumber(values[5]) then redis.call('DEL', KEYS[1]) end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisVerificationChallengeRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(VerificationChallenge challenge) {
        String key = key(challenge.requestId());
        redisTemplate.opsForHash().putAll(key, Map.of(
                "phoneDigest", challenge.phoneDigest(),
                "codeDigest", challenge.codeDigest(),
                "purpose", challenge.purpose().name(),
                "failures", "0",
                "maxFailures", Integer.toString(challenge.maxFailures())
        ));
        Duration ttl = Duration.between(OffsetDateTime.now(), challenge.expiresAt());
        redisTemplate.expire(key, ttl.isNegative() ? Duration.ZERO : ttl);
    }

    @Override
    public void delete(String requestId) {
        redisTemplate.delete(key(requestId));
    }

    @Override
    public boolean consume(String requestId, String phoneDigest, String codeDigest,
                           VerificationPurpose purpose) {
        Long result = redisTemplate.execute(CONSUME_SCRIPT, List.of(key(requestId)),
                phoneDigest, codeDigest, purpose.name());
        return result != null && result == 1L;
    }

    private String key(String requestId) {
        return "auth:otp:" + requestId;
    }
}
