package cn.jieyuc.luminova.messaging.core;

/**
 * 各 Adapter 共同使用的稳定消息头名称。
 */
public final class MessageHeaders {

    public static final String EVENT_ID = "x-event-id";
    public static final String EVENT_TYPE = "x-event-type";
    public static final String SCHEMA_VERSION = "x-schema-version";
    public static final String SOURCE = "x-source";
    public static final String TRACE_ID = "x-trace-id";

    private MessageHeaders() {
    }
}
