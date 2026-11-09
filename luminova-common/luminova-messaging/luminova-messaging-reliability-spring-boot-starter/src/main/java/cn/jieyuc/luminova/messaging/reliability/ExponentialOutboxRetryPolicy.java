package cn.jieyuc.luminova.messaging.reliability;

import java.time.Duration;
import java.time.Instant;

/**
 * 有上限的指数退避策略。
 *
 * <p>持续失败只降低重试频率，不自动丢弃业务事件。</p>
 */
public final class ExponentialOutboxRetryPolicy implements OutboxRetryPolicy {

    private final Duration initialBackoff;
    private final Duration maximumBackoff;

    public ExponentialOutboxRetryPolicy(Duration initialBackoff, Duration maximumBackoff) {
        this.initialBackoff = initialBackoff;
        this.maximumBackoff = maximumBackoff;
    }

    /**
     * 计算不超过上限的二次幂退避时间。
     *
     * @param attempts 失败次数
     * @param now 当前时间
     * @return 下一次投递时间
     */
    @Override
    public Instant nextRetryAt(int attempts, Instant now) {
        int safeExponent = Math.min(Math.max(attempts - 1, 0), 30);
        long multiplier = 1L << safeExponent;
        Duration calculated;
        try {
            calculated = initialBackoff.multipliedBy(multiplier);
        } catch (ArithmeticException exception) {
            calculated = maximumBackoff;
        }
        Duration delay = calculated.compareTo(maximumBackoff) > 0 ? maximumBackoff : calculated;
        return now.plus(delay);
    }
}
