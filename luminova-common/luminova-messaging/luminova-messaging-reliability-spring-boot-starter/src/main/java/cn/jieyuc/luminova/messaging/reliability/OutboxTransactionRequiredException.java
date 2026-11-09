package cn.jieyuc.luminova.messaging.reliability;

/**
 * 未在活动事务中追加 Outbox 事件时抛出的异常。
 */
public class OutboxTransactionRequiredException extends RuntimeException {

    public OutboxTransactionRequiredException() {
        super("追加 Outbox 事件必须处于活动的业务事务中");
    }
}
