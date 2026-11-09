package cn.jieyuc.luminova.messaging.api;

import java.util.concurrent.CompletableFuture;

/**
 * 业务侧消息发布端口。
 *
 * <p>与本地事务一致的可靠事件不得直接调用该端口，应使用 Outbox 追加端口。</p>
 */
public interface MessagePublisher {

    /**
     * 同步发布不依赖本地事务的一般消息。
     *
     * @param destination 逻辑目的地
     * @param message 消息信封
     * @return Broker 确认结果
     */
    MessagePublishResult publish(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message);

    /**
     * 异步发布一般消息，失败通过 Future 异常返回。
     *
     * @param destination 逻辑目的地
     * @param message 消息信封
     * @return 异步发布结果
     */
    CompletableFuture<MessagePublishResult> publishAsync(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message);
}
