package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.MessageMode;

import java.time.Instant;

/**
 * 已由部署配置解析的物理目的地。
 *
 * @param topicCode 逻辑 Topic
 * @param physicalTopic 物理 Topic
 * @param mode 消息模式
 * @param deliverAt 计划投递时间
 */
public record ResolvedDestination(
        String topicCode,
        String physicalTopic,
        MessageMode mode,
        Instant deliverAt) {
}
