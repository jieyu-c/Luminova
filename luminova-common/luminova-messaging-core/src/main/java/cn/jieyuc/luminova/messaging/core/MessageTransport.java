package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.MessagePublishResult;

import java.util.concurrent.CompletableFuture;

/**
 * 隔离厂商协议和客户端对象的传输 SPI。
 */
public interface MessageTransport extends AutoCloseable {

    /**
     * 返回当前 Adapter 的提供方。
     *
     * @return 提供方
     */
    TransportProvider provider();

    /**
     * 返回当前 Adapter 精确支持的高级能力。
     *
     * @return 能力声明
     */
    TransportCapabilities capabilities();

    /**
     * 同步发送编码后的消息。
     *
     * @param destination 物理目的地
     * @param message 编码消息
     * @return 发布结果
     */
    MessagePublishResult publish(ResolvedDestination destination, EncodedMessage message);

    /**
     * 异步发送编码后的消息。
     *
     * @param destination 物理目的地
     * @param message 编码消息
     * @return 异步发布结果
     */
    CompletableFuture<MessagePublishResult> publishAsync(
            ResolvedDestination destination,
            EncodedMessage message);

    /**
     * 释放厂商客户端资源。
     */
    @Override
    void close();
}
