package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageDestination;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import cn.jieyuc.luminova.messaging.api.MessageMode;
import cn.jieyuc.luminova.messaging.api.MessagePublishResult;
import cn.jieyuc.luminova.messaging.api.MessagePublisher;
import cn.jieyuc.luminova.messaging.core.exception.MessageTooLargeException;
import cn.jieyuc.luminova.messaging.core.exception.UnsupportedTransportCapabilityException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 平台无关的默认消息发布器。
 *
 * <p>负责统一校验、编码和能力检查，不承担业务事务一致性；可靠事件由 Outbox 调用本发布器。</p>
 */
public final class DefaultMessagePublisher implements MessagePublisher {

    private final MessageTransport transport;
    private final MessageCodec codec;
    private final MessageDestinationResolver destinationResolver;
    private final int maximumMessageBytes;

    public DefaultMessagePublisher(
            MessageTransport transport,
            MessageCodec codec,
            MessageDestinationResolver destinationResolver,
            int maximumMessageBytes) {
        this.transport = transport;
        this.codec = codec;
        this.destinationResolver = destinationResolver;
        this.maximumMessageBytes = maximumMessageBytes;
    }

    /**
     * 校验并同步发布一般消息。
     *
     * @param destination 逻辑目的地
     * @param message 消息信封
     * @return 发布结果
     */
    @Override
    public MessagePublishResult publish(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message) {
        PreparedMessage prepared = prepare(destination, message);
        return transport.publish(prepared.destination(), prepared.message());
    }

    /**
     * 校验并异步发布一般消息。
     *
     * @param destination 逻辑目的地
     * @param message 消息信封
     * @return 异步结果
     */
    @Override
    public CompletableFuture<MessagePublishResult> publishAsync(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message) {
        PreparedMessage prepared = prepare(destination, message);
        return transport.publishAsync(prepared.destination(), prepared.message());
    }

    /**
     * 在进入厂商 Adapter 前完成所有平台无关约束，避免不同 Adapter 行为漂移。
     */
    private PreparedMessage prepare(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> envelope) {
        validateCapabilities(destination, envelope);
        byte[] body = codec.encode(envelope);
        if (body.length > maximumMessageBytes) {
            throw new MessageTooLargeException(envelope.eventId(), body.length, maximumMessageBytes);
        }
        Map<String, String> properties = new LinkedHashMap<>(envelope.attributes());
        properties.put(MessageHeaders.EVENT_ID, envelope.eventId());
        properties.put(MessageHeaders.EVENT_TYPE, envelope.eventType());
        properties.put(MessageHeaders.SCHEMA_VERSION, Integer.toString(envelope.schemaVersion()));
        properties.put(MessageHeaders.SOURCE, envelope.source());
        if (envelope.traceId() != null && !envelope.traceId().isBlank()) {
            properties.put(MessageHeaders.TRACE_ID, envelope.traceId());
        }
        EncodedMessage message = new EncodedMessage(
                envelope.eventId(), envelope.eventType(), envelope.orderingKey(), body, properties);
        return new PreparedMessage(destinationResolver.resolve(destination), message);
    }

    /**
     * 对高级能力做显式校验，禁止不支持的平台静默降级。
     */
    private void validateCapabilities(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> envelope) {
        TransportCapabilities capabilities = transport.capabilities();
        if (destination.mode() == MessageMode.FIFO) {
            if (!capabilities.fifo()) {
                throw new UnsupportedTransportCapabilityException("FIFO");
            }
            if (envelope.orderingKey() == null || envelope.orderingKey().isBlank()) {
                throw new IllegalArgumentException("FIFO 消息必须提供 orderingKey");
            }
        }
        if (destination.mode() == MessageMode.DELAY && !capabilities.delayedDelivery()) {
            throw new UnsupportedTransportCapabilityException("DELAY");
        }
    }

    private record PreparedMessage(ResolvedDestination destination, EncodedMessage message) {
    }
}
