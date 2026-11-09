package cn.jieyuc.luminova.messaging.reliability;

/**
 * 本地幂等消费事务结果。
 */
public enum ConsumeExecutionResult {
    SUCCESS,
    DUPLICATE
}
