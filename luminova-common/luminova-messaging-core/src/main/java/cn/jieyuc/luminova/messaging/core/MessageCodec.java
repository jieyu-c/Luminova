package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;

/**
 * 消息信封序列化端口。
 */
public interface MessageCodec {

    /**
     * 序列化完整信封。
     *
     * @param envelope 消息信封
     * @return 编码结果
     */
    byte[] encode(MessageEnvelope<? extends IntegrationEvent> envelope);

    /**
     * 根据白名单载荷类型反序列化信封。
     *
     * @param body 消息正文
     * @param payloadType 载荷类型
     * @param <T> 事件类型
     * @return 消息信封
     */
    <T extends IntegrationEvent> MessageEnvelope<T> decode(byte[] body, Class<T> payloadType);
}
