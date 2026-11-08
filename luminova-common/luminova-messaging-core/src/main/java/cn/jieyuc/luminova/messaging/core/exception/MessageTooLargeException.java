package cn.jieyuc.luminova.messaging.core.exception;

/**
 * 编码后的消息超过 SDK 安全上限时抛出的异常。
 */
public class MessageTooLargeException extends RuntimeException {

    public MessageTooLargeException(String eventId, int actualBytes, int maximumBytes) {
        super("消息超过大小限制，eventId=" + eventId + ", actual=" + actualBytes + ", maximum=" + maximumBytes);
    }
}
