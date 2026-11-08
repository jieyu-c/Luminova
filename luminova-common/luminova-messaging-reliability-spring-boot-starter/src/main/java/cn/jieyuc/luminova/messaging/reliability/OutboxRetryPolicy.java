package cn.jieyuc.luminova.messaging.reliability;

import java.time.Instant;

/**
 * Outbox 发布失败后的退避策略。
 */
public interface OutboxRetryPolicy {

    /**
     * 计算下一次投递时间。
     *
     * @param attempts 本次失败后的累计次数
     * @param now 当前时间
     * @return 下一次投递时间
     */
    Instant nextRetryAt(int attempts, Instant now);
}
