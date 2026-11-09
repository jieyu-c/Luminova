package cn.jieyuc.luminova.messaging.reliability;

import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

/**
 * 将消费记录和业务 Handler 放入同一本地事务的幂等执行器。
 *
 * <p>事务提交后才允许 Adapter ACK；Handler 异常会回滚消费记录和业务修改。</p>
 */
public final class IdempotentMessageExecutor {

    private final ConsumedEventRepository repository;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public IdempotentMessageExecutor(
            ConsumedEventRepository repository,
            TransactionTemplate transactionTemplate,
            Clock clock) {
        this.repository = repository;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
    }

    /**
     * 首次事件执行 Handler，重复事件跳过业务副作用并安全确认。
     *
     * @param consumerGroup 逻辑消费组
     * @param envelope 消息信封
     * @param payloadDigest 消息正文摘要
     * @param handler 业务处理逻辑
     * @return 成功或重复结果
     */
    public ConsumeExecutionResult execute(
            String consumerGroup,
            MessageEnvelope<?> envelope,
            String payloadDigest,
            Runnable handler) {
        ConsumeExecutionResult result = transactionTemplate.execute(status -> {
            ConsumedEventEntity entity = new ConsumedEventEntity();
            entity.setConsumerGroup(consumerGroup);
            entity.setEventId(envelope.eventId());
            entity.setEventType(envelope.eventType());
            entity.setPayloadDigest(payloadDigest);
            entity.setConsumedAt(clock.instant());
            if (!repository.tryInsert(entity)) {
                return ConsumeExecutionResult.DUPLICATE;
            }
            handler.run();
            return ConsumeExecutionResult.SUCCESS;
        });
        if (result == null) {
            throw new IllegalStateException("消息消费事务未返回结果");
        }
        return result;
    }
}
