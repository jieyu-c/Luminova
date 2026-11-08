package cn.jieyuc.luminova.messaging.api;

import java.time.Instant;

/**
 * 一次消息消费的技术上下文。
 *
 * @param eventId 事件 ID
 * @param traceId 调用链 ID
 * @param deliveryAttempt 当前投递次数
 * @param receivedAt SDK 接收时间
 */
public record MessageContext(
        String eventId,
        String traceId,
        int deliveryAttempt,
        Instant receivedAt) {
}
