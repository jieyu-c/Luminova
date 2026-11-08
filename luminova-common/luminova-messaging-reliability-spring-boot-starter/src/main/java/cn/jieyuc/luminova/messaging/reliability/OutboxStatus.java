package cn.jieyuc.luminova.messaging.reliability;

/**
 * Outbox 事件发布状态。
 */
public enum OutboxStatus {
    PENDING,
    CLAIMED,
    PUBLISHED
}
