package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageContext;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import cn.jieyuc.luminova.messaging.api.MessageHandler;
import cn.jieyuc.luminova.messaging.api.MessageHandlerDescriptor;
import cn.jieyuc.luminova.messaging.core.exception.MessageConfigurationException;

import java.security.MessageDigest;
import java.time.Clock;
import java.util.HexFormat;

/**
 * 平台无关的消息反序列化、版本校验和 Handler 分发器。
 */
public final class MessageDispatcher {

    private final MessageHandlerRegistry registry;
    private final MessageCodec codec;
    private final MessageConsumptionExecutor consumptionExecutor;
    private final Clock clock;

    public MessageDispatcher(
            MessageHandlerRegistry registry,
            MessageCodec codec,
            MessageConsumptionExecutor consumptionExecutor,
            Clock clock) {
        this.registry = registry;
        this.codec = codec;
        this.consumptionExecutor = consumptionExecutor;
        this.clock = clock;
    }

    /**
     * 根据消费组、逻辑 Topic 和消息头分发事件；任何异常都应由 Adapter 转换为消费失败。
     */
    public void dispatch(String groupCode, String topicCode, IncomingMessage incoming) {
        String eventType = requireProperty(incoming, MessageHeaders.EVENT_TYPE);
        MessageHandler<?> handler = registry.require(groupCode, topicCode, eventType);
        dispatchUnchecked(handler, incoming);
    }

    private <T extends IntegrationEvent> void dispatchTyped(
            MessageHandler<T> handler,
            IncomingMessage incoming) {
        MessageEnvelope<T> envelope = codec.decode(incoming.body(), handler.payloadType());
        MessageHandlerDescriptor descriptor = handler.descriptor();
        if (!descriptor.supports(envelope.schemaVersion())) {
            throw new MessageConfigurationException(
                    "Handler 不支持事件版本: " + envelope.eventType() + "@" + envelope.schemaVersion());
        }
        MessageContext context = new MessageContext(
                envelope.eventId(), envelope.traceId(), incoming.deliveryAttempt(), clock.instant());
        consumptionExecutor.execute(
                descriptor,
                envelope,
                digest(incoming.body()),
                () -> handler.handle(context, envelope.payload()));
    }

    @SuppressWarnings("unchecked")
    private void dispatchUnchecked(MessageHandler<?> handler, IncomingMessage incoming) {
        dispatchTyped((MessageHandler<IntegrationEvent>) handler, incoming);
    }

    private String requireProperty(IncomingMessage message, String key) {
        String value = message.properties().get(key);
        if (value == null || value.isBlank()) {
            throw new MessageConfigurationException("消息缺少必要属性: " + key);
        }
        return value;
    }

    private String digest(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (Exception exception) {
            throw new IllegalStateException("消息摘要计算失败", exception);
        }
    }
}
