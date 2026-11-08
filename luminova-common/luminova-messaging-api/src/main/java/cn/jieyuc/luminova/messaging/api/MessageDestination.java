package cn.jieyuc.luminova.messaging.api;

import java.time.Instant;
import java.util.Objects;

/**
 * 与物理消息平台无关的逻辑投递目的地。
 *
 * @param topicCode 逻辑 Topic 代码
 * @param mode 投递模式
 * @param deliverAt 延迟投递时间，非延迟消息必须为空
 */
public record MessageDestination(String topicCode, MessageMode mode, Instant deliverAt) {

    /**
     * 校验目的地的模式与参数保持一致。
     */
    public MessageDestination {
        if (topicCode == null || topicCode.isBlank()) {
            throw new IllegalArgumentException("逻辑 Topic 不能为空");
        }
        Objects.requireNonNull(mode, "消息模式不能为空");
        if (mode != MessageMode.DELAY && deliverAt != null) {
            throw new IllegalArgumentException("只有延迟消息可以指定投递时间");
        }
        if (mode == MessageMode.DELAY && deliverAt == null) {
            throw new IllegalArgumentException("延迟消息必须指定投递时间");
        }
    }

    /**
     * 创建普通消息目的地。
     *
     * @param topicCode 逻辑 Topic
     * @return 普通消息目的地
     */
    public static MessageDestination normal(String topicCode) {
        return new MessageDestination(topicCode, MessageMode.NORMAL, null);
    }

    /**
     * 创建 FIFO 消息目的地。
     *
     * @param topicCode 逻辑 Topic
     * @return FIFO 消息目的地
     */
    public static MessageDestination fifo(String topicCode) {
        return new MessageDestination(topicCode, MessageMode.FIFO, null);
    }

    /**
     * 创建延迟消息目的地。
     *
     * @param topicCode 逻辑 Topic
     * @param deliverAt 计划投递时间
     * @return 延迟消息目的地
     */
    public static MessageDestination delayed(String topicCode, Instant deliverAt) {
        return new MessageDestination(topicCode, MessageMode.DELAY, deliverAt);
    }
}
