package cn.jieyuc.luminova.messaging.core.exception;

/**
 * 消息信封无法安全编解码时抛出的异常。
 */
public class MessageSerializationException extends RuntimeException {

    private final String eventId;

    public MessageSerializationException(String eventId, Throwable cause) {
        super("消息信封编解码失败", cause);
        this.eventId = eventId;
    }

    /**
     * 返回可用于诊断且不包含正文的事件 ID。
     *
     * @return 事件 ID
     */
    public String eventId() {
        return eventId;
    }
}
