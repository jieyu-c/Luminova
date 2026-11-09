package cn.jieyuc.luminova.messaging.reliability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Outbox 指数退避边界测试。
 */
class ExponentialOutboxRetryPolicyTest {

    @Test
    @DisplayName("重试延迟按二次幂增长并受最大值限制")
    void backoffIsCapped() {
        ExponentialOutboxRetryPolicy policy = new ExponentialOutboxRetryPolicy(
                Duration.ofSeconds(1), Duration.ofSeconds(10));
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        assertEquals(now.plusSeconds(1), policy.nextRetryAt(1, now));
        assertEquals(now.plusSeconds(8), policy.nextRetryAt(4, now));
        assertEquals(now.plusSeconds(10), policy.nextRetryAt(10, now));
    }
}
