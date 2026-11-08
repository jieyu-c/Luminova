package cn.jieyuc.luminova.messaging.core.exception;

/**
 * 当前 Adapter 无法表达请求的高级消息语义时抛出的异常。
 */
public class UnsupportedTransportCapabilityException extends RuntimeException {

    public UnsupportedTransportCapabilityException(String capability) {
        super("当前消息传输不支持能力: " + capability);
    }
}
