package cn.jieyuc.luminova.messaging.reliability;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageDestination;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import cn.jieyuc.luminova.messaging.api.OutboxEventAppender;
import cn.jieyuc.luminova.messaging.core.MessageCodec;
import cn.jieyuc.luminova.messaging.core.MessageDestinationResolver;
import cn.jieyuc.luminova.messaging.core.MessageHeaders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 将业务事实和待发布消息写入同一本地事务的 Outbox Appender。
 *
 * <p>本类不启动事务；调用方应用服务必须先建立事务，避免产生虚假的原子性承诺。</p>
 */
public final class TransactionalOutboxEventAppender implements OutboxEventAppender {

    private final OutboxEventRepository repository;
    private final MessageCodec codec;
    private final MessageDestinationResolver destinationResolver;
    private final ObjectMapper objectMapper;
    private final Supplier<Long> identifierSupplier;
    private final Clock clock;

    public TransactionalOutboxEventAppender(
            OutboxEventRepository repository,
            MessageCodec codec,
            MessageDestinationResolver destinationResolver,
            ObjectMapper objectMapper,
            Supplier<Long> identifierSupplier,
            Clock clock) {
        this.repository = repository;
        this.codec = codec;
        this.destinationResolver = destinationResolver;
        this.objectMapper = objectMapper;
        this.identifierSupplier = identifierSupplier;
        this.clock = clock;
    }

    /**
     * 在现有业务事务中追加事件；事务回滚时该记录必须一并回滚。
     *
     * @param destination 逻辑目的地
     * @param message 完整事件信封
     */
    @Override
    public void append(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new OutboxTransactionRequiredException();
        }
        destinationResolver.resolve(destination);
        byte[] envelopeBytes = codec.encode(message);
        Map<String, String> properties = buildProperties(message);
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(identifierSupplier.get());
        entity.setEventId(message.eventId());
        entity.setTopicCode(destination.topicCode());
        entity.setMessageMode(destination.mode().name());
        entity.setEventType(message.eventType());
        entity.setSchemaVersion(message.schemaVersion());
        entity.setOrderingKey(message.orderingKey());
        entity.setDeliverAt(destination.deliverAt());
        entity.setEnvelopeJson(new String(envelopeBytes, StandardCharsets.UTF_8));
        entity.setPropertiesJson(writeProperties(properties));
        entity.setStatus(OutboxStatus.PENDING.name());
        entity.setAttempts(0);
        entity.setNextAttemptAt(clock.instant());
        entity.setCreatedAt(clock.instant());
        entity.setVersion(0);
        repository.append(entity);
    }

    private Map<String, String> buildProperties(
            MessageEnvelope<? extends IntegrationEvent> message) {
        Map<String, String> properties = new LinkedHashMap<>(message.attributes());
        properties.put(MessageHeaders.EVENT_ID, message.eventId());
        properties.put(MessageHeaders.EVENT_TYPE, message.eventType());
        properties.put(MessageHeaders.SCHEMA_VERSION, Integer.toString(message.schemaVersion()));
        properties.put(MessageHeaders.SOURCE, message.source());
        if (message.traceId() != null && !message.traceId().isBlank()) {
            properties.put(MessageHeaders.TRACE_ID, message.traceId());
        }
        return properties;
    }

    private String writeProperties(Map<String, String> properties) {
        try {
            return objectMapper.writeValueAsString(properties);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Outbox 消息属性序列化失败", exception);
        }
    }
}
