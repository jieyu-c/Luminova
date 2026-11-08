package cn.jieyuc.luminova.messaging.rocketmq;

import cn.jieyuc.luminova.messaging.api.MessagePublisher;
import cn.jieyuc.luminova.messaging.core.DefaultMessagePublisher;
import cn.jieyuc.luminova.messaging.core.DirectMessageConsumptionExecutor;
import cn.jieyuc.luminova.messaging.core.JacksonMessageCodec;
import cn.jieyuc.luminova.messaging.core.MapMessageDestinationResolver;
import cn.jieyuc.luminova.messaging.core.MessageCodec;
import cn.jieyuc.luminova.messaging.core.MessageConsumptionExecutor;
import cn.jieyuc.luminova.messaging.core.MessageDispatcher;
import cn.jieyuc.luminova.messaging.core.MessageHandlerRegistry;
import cn.jieyuc.luminova.messaging.core.MessageDestinationResolver;
import cn.jieyuc.luminova.messaging.core.MessageTransport;
import cn.jieyuc.luminova.messaging.core.exception.MessageConfigurationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import cn.jieyuc.luminova.messaging.api.MessageHandler;

import java.time.Clock;

/**
 * RocketMQ Messaging SDK 自动装配入口。
 *
 * <p>启用后配置缺失会直接阻止应用启动，避免可靠消息被静默禁用。</p>
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnClass(ClientServiceProvider.class)
@ConditionalOnProperty(prefix = "luminova.messaging.rocketmq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RocketMqMessagingProperties.class)
public class RocketMqMessagingAutoConfiguration {

    /**
     * 提供可被测试替换的系统时钟。
     *
     * @return UTC 时钟
     */
    @Bean
    @ConditionalOnMissingBean
    public Clock messagingClock() {
        return Clock.systemUTC();
    }

    /**
     * 创建 RocketMQ 官方客户端服务提供者。
     *
     * @return 服务提供者
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientServiceProvider rocketMqClientServiceProvider() {
        return ClientServiceProvider.loadService();
    }

    /**
     * 创建 RocketMQ 客户端通用配置。
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientConfiguration rocketMqClientConfiguration(RocketMqMessagingProperties properties) {
        validate(properties);
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoints())
                .setRequestTimeout(properties.getRequestTimeout())
                .enableSsl(properties.isSslEnabled());
        if (properties.getNamespace() != null && !properties.getNamespace().isBlank()) {
            builder.setNamespace(properties.getNamespace());
        }
        return builder.build();
    }

    /**
     * 创建统一 JSON 编解码器。
     *
     * @param objectMapper 应用 ObjectMapper
     * @return 消息编解码器
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageCodec messageCodec(ObjectMapper objectMapper) {
        return new JacksonMessageCodec(objectMapper);
    }

    /**
     * 创建逻辑 Topic 解析器。
     *
     * @param properties RocketMQ 配置
     * @return 目的地解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageDestinationResolver messageDestinationResolver(
            RocketMqMessagingProperties properties) {
        if (properties.getTopics().isEmpty()) {
            throw new MessageConfigurationException("至少需要配置一个逻辑 Topic 映射");
        }
        return new MapMessageDestinationResolver(properties.getTopics());
    }

    /**
     * 创建并托管 RocketMQ Producer。
     *
     * @param provider 官方客户端提供者
     * @param properties RocketMQ 配置
     * @return Producer
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public Producer rocketMqProducer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqMessagingProperties properties) {
        try {
            return provider.newProducerBuilder()
                    .setClientConfiguration(clientConfiguration)
                    .setTopics(properties.getTopics().values().toArray(String[]::new))
                    .setMaxAttempts(properties.getMaximumAttempts())
                    .build();
        } catch (ClientException exception) {
            throw new MessageConfigurationException("RocketMQ Producer 初始化失败: " + exception.getMessage());
        }
    }

    /**
     * 注册并校验业务 Handler。
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageHandlerRegistry messageHandlerRegistry(
            java.util.List<MessageHandler<?>> handlers) {
        return new MessageHandlerRegistry(handlers);
    }

    /**
     * 在未启用可靠性模块时提供直接消费执行策略。
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageConsumptionExecutor messageConsumptionExecutor() {
        return new DirectMessageConsumptionExecutor();
    }

    /**
     * 创建平台无关消息分发器。
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageDispatcher messageDispatcher(
            MessageHandlerRegistry registry,
            MessageCodec codec,
            MessageConsumptionExecutor executor,
            Clock clock) {
        return new MessageDispatcher(registry, codec, executor, clock);
    }

    /**
     * 当业务注册 Handler 时创建并托管 PushConsumer。
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public RocketMqConsumerContainer rocketMqConsumerContainer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            RocketMqMessagingProperties properties,
            MessageHandlerRegistry registry,
            MessageDispatcher dispatcher) {
        return new RocketMqConsumerContainer(
                provider, clientConfiguration, properties, registry, dispatcher);
    }

    /**
     * 创建厂商传输适配器。
     *
     * @return RocketMQ 传输
     */
    @Bean
    @ConditionalOnMissingBean(MessageTransport.class)
    public MessageTransport rocketMqMessageTransport(
            ClientServiceProvider provider,
            Producer producer,
            Clock clock) {
        return new RocketMqMessageTransport(provider, producer, clock);
    }

    /**
     * 创建业务侧统一发布端口。
     *
     * @return 消息发布器
     */
    @Bean
    @ConditionalOnMissingBean
    public MessagePublisher messagePublisher(
            MessageTransport transport,
            MessageCodec codec,
            MessageDestinationResolver resolver,
            RocketMqMessagingProperties properties) {
        return new DefaultMessagePublisher(
                transport, codec, resolver, properties.getMaximumMessageBytes());
    }

    private void validate(RocketMqMessagingProperties properties) {
        if (properties.getEndpoints() == null || properties.getEndpoints().isBlank()) {
            throw new MessageConfigurationException("RocketMQ endpoints 不能为空");
        }
        if (properties.getMaximumAttempts() < 1) {
            throw new MessageConfigurationException("RocketMQ 发送尝试次数必须大于零");
        }
    }
}
