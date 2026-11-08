package cn.jieyuc.luminova.messaging.api;

/**
 * 在调用方本地事务内追加待发布事件的端口。
 */
public interface OutboxEventAppender {

    /**
     * 将事件与业务数据原子持久化；不存在活动事务时必须失败。
     *
     * @param destination 逻辑目的地
     * @param message 事件信封
     */
    void append(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message);
}
