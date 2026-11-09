package cn.jieyuc.luminova.messaging.rocketmq;

import cn.jieyuc.luminova.messaging.api.MessageHandler;
import cn.jieyuc.luminova.messaging.api.MessageHandlerDescriptor;
import cn.jieyuc.luminova.messaging.core.IncomingMessage;
import cn.jieyuc.luminova.messaging.core.MessageDispatcher;
import cn.jieyuc.luminova.messaging.core.MessageHandlerRegistry;
import cn.jieyuc.luminova.messaging.core.exception.MessageConfigurationException;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 按逻辑消费组聚合并托管 RocketMQ PushConsumer 生命周期。
 *
 * <p>业务 Handler 分散在各业务服务，本容器只负责订阅、协议转换和分发。</p>
 */
public final class RocketMqConsumerContainer implements AutoCloseable {

    private final List<PushConsumer> consumers;

    public RocketMqConsumerContainer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqMessagingProperties properties,
            MessageHandlerRegistry registry,
            MessageDispatcher dispatcher) {
        this.consumers = createConsumers(provider, clientConfiguration, properties, registry, dispatcher);
    }

    /**
     * 关闭所有 Consumer；尝试完整释放后再报告首个关闭异常。
     */
    @Override
    public void close() {
        IOException first = null;
        for (PushConsumer consumer : consumers) {
            try {
                consumer.close();
            } catch (IOException exception) {
                if (first == null) {
                    first = exception;
                }
            }
        }
        if (first != null) {
            throw new IllegalStateException("RocketMQ Consumer 关闭失败", first);
        }
    }

    private List<PushConsumer> createConsumers(
            ClientServiceProvider provider,
            ClientConfiguration configuration,
            RocketMqMessagingProperties properties,
            MessageHandlerRegistry registry,
            MessageDispatcher dispatcher) {
        Map<String, Map<String, String>> subscriptionsByGroup = new LinkedHashMap<>();
        for (MessageHandler<?> handler : registry.all()) {
            MessageHandlerDescriptor descriptor = handler.descriptor();
            String physicalTopic = requireMapping(properties.getTopics(), descriptor.topicCode(), "Topic");
            subscriptionsByGroup
                    .computeIfAbsent(descriptor.consumerGroupCode(), ignored -> new LinkedHashMap<>())
                    .put(physicalTopic, descriptor.topicCode());
        }
        List<PushConsumer> created = new ArrayList<>();
        subscriptionsByGroup.forEach((groupCode, subscriptions) -> created.add(
                createConsumer(provider, configuration, properties, dispatcher, groupCode, subscriptions)));
        return List.copyOf(created);
    }

    private PushConsumer createConsumer(
            ClientServiceProvider provider,
            ClientConfiguration configuration,
            RocketMqMessagingProperties properties,
            MessageDispatcher dispatcher,
            String groupCode,
            Map<String, String> subscriptions) {
        String physicalGroup = requireMapping(properties.getConsumerGroups(), groupCode, "Consumer Group");
        Map<String, FilterExpression> expressions = new LinkedHashMap<>();
        subscriptions.keySet().forEach(topic -> expressions.put(topic, FilterExpression.SUB_ALL));
        try {
            return provider.newPushConsumerBuilder()
                    .setClientConfiguration(configuration)
                    .setConsumerGroup(physicalGroup)
                    .setSubscriptionExpressions(expressions)
                    .setMessageListener(message -> consume(dispatcher, groupCode, subscriptions, message))
                    .build();
        } catch (ClientException exception) {
            throw new MessageConfigurationException("RocketMQ Consumer 初始化失败: " + exception.getMessage());
        }
    }

    /**
     * 只有完整分发并提交后返回成功，任何异常都交由 Broker 重试。
     */
    private ConsumeResult consume(
            MessageDispatcher dispatcher,
            String groupCode,
            Map<String, String> subscriptions,
            MessageView messageView) {
        try {
            String topicCode = subscriptions.get(messageView.getTopic());
            if (topicCode == null) {
                return ConsumeResult.FAILURE;
            }
            ByteBuffer body = messageView.getBody().asReadOnlyBuffer();
            byte[] bytes = new byte[body.remaining()];
            body.get(bytes);
            dispatcher.dispatch(
                    groupCode,
                    topicCode,
                    new IncomingMessage(bytes, messageView.getProperties(), messageView.getDeliveryAttempt()));
            return ConsumeResult.SUCCESS;
        } catch (RuntimeException exception) {
            return ConsumeResult.FAILURE;
        }
    }

    private String requireMapping(Map<String, String> mappings, String code, String type) {
        String value = mappings.get(code);
        if (value == null || value.isBlank()) {
            throw new MessageConfigurationException(type + " 未配置物理映射: " + code);
        }
        return value;
    }
}
