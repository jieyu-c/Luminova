package cn.jieyuc.luminova.messaging.api;

import java.time.Instant;

/**
 * 隐藏厂商回执细节的发布结果。
 *
 * @param eventId 事件 ID
 * @param provider 当前传输提供方
 * @param providerMessageId 仅用于诊断的厂商消息 ID
 * @param publishedAt Broker 确认时间
 */
public record MessagePublishResult(
        String eventId,
        String provider,
        String providerMessageId,
        Instant publishedAt) {
}
