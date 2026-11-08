package cn.jieyuc.luminova.messaging.api;

/**
 * 消费者的幂等保障要求。
 */
public enum IdempotencyMode {
    REQUIRED,
    DISABLED
}
