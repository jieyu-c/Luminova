package cn.jieyuc.luminova.messaging.core.exception;

/**
 * 厂商客户端最终未能确认发布时抛出的统一异常。
 */
public class MessagePublishException extends RuntimeException {

    public MessagePublishException(String eventId, Throwable cause) {
        super("消息发布失败，eventId=" + eventId, cause);
    }
}
