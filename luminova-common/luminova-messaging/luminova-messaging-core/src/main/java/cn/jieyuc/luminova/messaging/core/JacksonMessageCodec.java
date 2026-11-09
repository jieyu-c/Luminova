package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import cn.jieyuc.luminova.messaging.core.exception.MessageSerializationException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 使用显式白名单载荷类型的 JSON 信封编解码器。
 *
 * <p>本实现不启用任意多态反序列化，避免消息正文构造任意 Java 类型。</p>
 */
public final class JacksonMessageCodec implements MessageCodec {

    private final ObjectMapper objectMapper;

    public JacksonMessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将完整信封编码为 UTF-8 JSON。
     *
     * @param envelope 消息信封
     * @return JSON 字节
     */
    @Override
    public byte[] encode(MessageEnvelope<? extends IntegrationEvent> envelope) {
        try {
            return objectMapper.writeValueAsBytes(envelope);
        } catch (Exception exception) {
            throw new MessageSerializationException(envelope.eventId(), exception);
        }
    }

    /**
     * 仅根据已注册 Handler 提供的类型还原信封。
     *
     * @param body JSON 正文
     * @param payloadType 已注册载荷类型
     * @param <T> 事件类型
     * @return 还原后的信封
     */
    @Override
    public <T extends IntegrationEvent> MessageEnvelope<T> decode(byte[] body, Class<T> payloadType) {
        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(MessageEnvelope.class, payloadType);
            return objectMapper.readValue(body, type);
        } catch (Exception exception) {
            throw new MessageSerializationException(null, exception);
        }
    }
}
