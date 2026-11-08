package cn.jieyuc.luminova.messaging.rocketmq;

import cn.jieyuc.luminova.messaging.api.MessageMode;
import cn.jieyuc.luminova.messaging.api.MessagePublishResult;
import cn.jieyuc.luminova.messaging.core.EncodedMessage;
import cn.jieyuc.luminova.messaging.core.MessageTransport;
import cn.jieyuc.luminova.messaging.core.ResolvedDestination;
import cn.jieyuc.luminova.messaging.core.TransportCapabilities;
import cn.jieyuc.luminova.messaging.core.TransportProvider;
import cn.jieyuc.luminova.messaging.core.exception.MessagePublishException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * 使用 RocketMQ 5.x gRPC Java Client 的传输适配器。
 *
 * <p>本类只进行协议映射和错误转换，不决定事件是否应当发布，也不实现 Outbox。</p>
 */
public final class RocketMqMessageTransport implements MessageTransport {

    private final ClientServiceProvider serviceProvider;
    private final Producer producer;
    private final Clock clock;

    public RocketMqMessageTransport(
            ClientServiceProvider serviceProvider,
            Producer producer,
            Clock clock) {
        this.serviceProvider = serviceProvider;
        this.producer = producer;
        this.clock = clock;
    }

    /**
     * 返回 RocketMQ 提供方。
     *
     * @return RocketMQ
     */
    @Override
    public TransportProvider provider() {
        return TransportProvider.ROCKETMQ;
    }

    /**
     * 声明 RocketMQ 5.x Adapter 当前开放的能力。
     *
     * @return 能力声明
     */
    @Override
    public TransportCapabilities capabilities() {
        return new TransportCapabilities(true, true, true, true, false);
    }

    /**
     * 同步发送消息并将厂商异常转换为 SDK 异常。
     *
     * @param destination 物理目的地
     * @param message 编码消息
     * @return 发布结果
     */
    @Override
    public MessagePublishResult publish(ResolvedDestination destination, EncodedMessage message) {
        try {
            SendReceipt receipt = producer.send(toRocketMqMessage(destination, message));
            return toResult(message.eventId(), receipt);
        } catch (Exception exception) {
            throw new MessagePublishException(message.eventId(), exception);
        }
    }

    /**
     * 异步发送消息，完成阶段统一映射异常和回执。
     *
     * @param destination 物理目的地
     * @param message 编码消息
     * @return 异步结果
     */
    @Override
    public CompletableFuture<MessagePublishResult> publishAsync(
            ResolvedDestination destination,
            EncodedMessage message) {
        return producer.sendAsync(toRocketMqMessage(destination, message))
                .handle((receipt, exception) -> {
                    if (exception != null) {
                        throw new CompletionException(new MessagePublishException(message.eventId(), exception));
                    }
                    return toResult(message.eventId(), receipt);
                });
    }

    /**
     * 关闭 Producer；关闭失败转换为不可恢复的资源释放异常。
     */
    @Override
    public void close() {
        try {
            producer.close();
        } catch (IOException exception) {
            throw new IllegalStateException("RocketMQ Producer 关闭失败", exception);
        }
    }

    /**
     * 将平台无关模型映射为 RocketMQ 消息，高级模式只在这里接触厂商 API。
     */
    private Message toRocketMqMessage(ResolvedDestination destination, EncodedMessage message) {
        MessageBuilder builder = serviceProvider.newMessageBuilder()
                .setTopic(destination.physicalTopic())
                .setBody(message.body())
                .setTag(message.eventType())
                .setKeys(message.eventId());
        message.properties().forEach(builder::addProperty);
        if (destination.mode() == MessageMode.FIFO) {
            builder.setMessageGroup(message.orderingKey());
        }
        if (destination.mode() == MessageMode.DELAY) {
            builder.setDeliveryTimestamp(destination.deliverAt().toEpochMilli());
        }
        return builder.build();
    }

    private MessagePublishResult toResult(String eventId, SendReceipt receipt) {
        return new MessagePublishResult(
                eventId,
                provider().name(),
                receipt.getMessageId().toString(),
                clock.instant());
    }
}
