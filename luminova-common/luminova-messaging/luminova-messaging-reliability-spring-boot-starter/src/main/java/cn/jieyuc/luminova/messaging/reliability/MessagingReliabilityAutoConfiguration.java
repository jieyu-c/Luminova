package cn.jieyuc.luminova.messaging.reliability;

import cn.jieyuc.luminova.messaging.api.OutboxEventAppender;
import cn.jieyuc.luminova.messaging.core.MessageCodec;
import cn.jieyuc.luminova.messaging.core.MessageConsumptionExecutor;
import cn.jieyuc.luminova.messaging.core.MessageDestinationResolver;
import cn.jieyuc.luminova.messaging.core.MessageTransport;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.InetAddress;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Messaging 可靠性组件自动装配。
 *
 * <p>组件依赖调用服务自己的数据源和本地事务，不建立中心化 Outbox 服务。</p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "luminova.messaging.reliability", name = "enabled", havingValue = "true")
@ConditionalOnBean({MessageTransport.class, MessageCodec.class, MessageDestinationResolver.class})
@EnableConfigurationProperties(MessagingReliabilityProperties.class)
@MapperScan(basePackageClasses = {OutboxEventMapper.class, ConsumedEventMapper.class})
public class MessagingReliabilityAutoConfiguration {

    /**
     * 在其他 Adapter 未提供时创建可靠性组件使用的 UTC 时钟。
     */
    @Bean
    @ConditionalOnMissingBean
    public Clock messagingReliabilityClock() {
        return Clock.systemUTC();
    }

    /**
     * 在应用未配置 MyBatis-Plus 拦截器时提供乐观锁能力。
     *
     * @return MyBatis-Plus 拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor messagingMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 创建 Outbox 仓储适配器。
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxEventRepository outboxEventRepository(OutboxEventMapper mapper) {
        return new MybatisPlusOutboxEventRepository(mapper);
    }

    /**
     * 创建消费幂等仓储适配器。
     */
    @Bean
    @ConditionalOnMissingBean
    public ConsumedEventRepository consumedEventRepository(ConsumedEventMapper mapper) {
        return new MybatisPlusConsumedEventRepository(mapper);
    }

    /**
     * 创建 Outbox 内部主键生成器。
     */
    @Bean
    @ConditionalOnMissingBean
    public IdentifierGenerator messagingIdentifierGenerator() {
        return DefaultIdentifierGenerator.getInstance();
    }

    /**
     * 创建要求调用方已有事务的 Outbox 追加端口。
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxEventAppender outboxEventAppender(
            OutboxEventRepository repository,
            MessageCodec codec,
            MessageDestinationResolver resolver,
            ObjectMapper objectMapper,
            IdentifierGenerator identifierGenerator,
            Clock clock) {
        Supplier<Long> identifierSupplier = () -> identifierGenerator.nextId(null).longValue();
        return new TransactionalOutboxEventAppender(
                repository, codec, resolver, objectMapper, identifierSupplier, clock);
    }

    /**
     * 创建 Outbox 退避策略。
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxRetryPolicy outboxRetryPolicy(MessagingReliabilityProperties properties) {
        return new ExponentialOutboxRetryPolicy(
                properties.getInitialBackoff(), properties.getMaximumBackoff());
    }

    /**
     * 创建可由应用定时器触发的 Outbox Worker。
     *
     * <p>Starter 不强制启用调度框架，避免与宿主服务的线程和调度治理冲突。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxPublisherWorker outboxPublisherWorker(
            OutboxEventRepository repository,
            MessageTransport transport,
            MessageDestinationResolver resolver,
            ObjectMapper objectMapper,
            OutboxRetryPolicy retryPolicy,
            Clock clock,
            MessagingReliabilityProperties properties,
            @Value("${spring.application.name:application}") String applicationName) {
        return new OutboxPublisherWorker(
                repository,
                transport,
                resolver,
                objectMapper,
                retryPolicy,
                clock,
                resolveInstanceId(properties, applicationName),
                properties.getBatchSize(),
                properties.getLeaseDuration());
    }

    /**
     * 创建消费幂等事务执行器。
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentMessageExecutor idempotentMessageExecutor(
            ConsumedEventRepository repository,
            PlatformTransactionManager transactionManager,
            Clock clock) {
        return new IdempotentMessageExecutor(
                repository, new TransactionTemplate(transactionManager), clock);
    }

    /**
     * 用数据库事务幂等执行器替换直接消费策略。
     */
    @Bean
    @ConditionalOnMissingBean(MessageConsumptionExecutor.class)
    public MessageConsumptionExecutor reliableMessageConsumptionExecutor(
            IdempotentMessageExecutor executor) {
        return (descriptor, envelope, payloadDigest, handler) ->
                executor.execute(
                        descriptor.consumerGroupCode(), envelope, payloadDigest, handler);
    }

    private String resolveInstanceId(
            MessagingReliabilityProperties properties,
            String applicationName) {
        if (properties.getInstanceId() != null && !properties.getInstanceId().isBlank()) {
            return properties.getInstanceId();
        }
        try {
            return applicationName + "-" + InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID();
        } catch (Exception exception) {
            return applicationName + "-" + UUID.randomUUID();
        }
    }
}
