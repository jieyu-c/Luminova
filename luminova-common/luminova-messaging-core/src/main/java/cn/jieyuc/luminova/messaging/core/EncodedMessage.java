package cn.jieyuc.luminova.messaging.core;

import java.util.Map;

/**
 * 已完成安全序列化、可交给厂商 Adapter 的消息。
 *
 * @param eventId 事件 ID
 * @param eventType 事件类型
 * @param orderingKey 顺序键
 * @param body 序列化正文
 * @param properties 非敏感消息属性
 */
public record EncodedMessage(
        String eventId,
        String eventType,
        String orderingKey,
        byte[] body,
        Map<String, String> properties) {

    /**
     * 防止调用方修改正文和属性。
     */
    public EncodedMessage {
        body = body.clone();
        properties = Map.copyOf(properties);
    }

    @Override
    public byte[] body() {
        return body.clone();
    }
}
