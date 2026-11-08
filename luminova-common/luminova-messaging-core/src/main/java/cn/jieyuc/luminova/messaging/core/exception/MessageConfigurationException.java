package cn.jieyuc.luminova.messaging.core.exception;

/**
 * 消息基础设施配置不完整或冲突时抛出的异常。
 */
public class MessageConfigurationException extends RuntimeException {

    public MessageConfigurationException(String message) {
        super(message);
    }
}
