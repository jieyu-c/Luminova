package cn.jieyuc.luminova.messaging.core;

import java.util.Map;

/**
 * 厂商 Consumer 接收到的平台无关消息视图。
 *
 * @param body 消息正文
 * @param properties 消息属性
 * @param deliveryAttempt 投递次数
 */
public record IncomingMessage(
        byte[] body,
        Map<String, String> properties,
        int deliveryAttempt) {

    public IncomingMessage {
        body = body.clone();
        properties = Map.copyOf(properties);
    }

    @Override
    public byte[] body() {
        return body.clone();
    }
}
