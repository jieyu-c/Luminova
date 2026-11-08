package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 统一生成事件标识与时间的信封工厂。
 */
public final class DefaultMessageEnvelopeFactory {

    private final Clock clock;
    private final Supplier<String> eventIdSupplier;

    public DefaultMessageEnvelopeFactory(Clock clock, Supplier<String> eventIdSupplier) {
        this.clock = clock;
        this.eventIdSupplier = eventIdSupplier;
    }

    /**
     * 使用默认 UUID 事件标识创建工厂。
     *
     * @param clock 时间源
     * @return 信封工厂
     */
    public static DefaultMessageEnvelopeFactory withUuid(Clock clock) {
        return new DefaultMessageEnvelopeFactory(clock, () -> UUID.randomUUID().toString());
    }

    /**
     * 创建普通事件信封；事件 ID 在创建后不得因重试而变化。
     *
     * @param source 来源服务
     * @param subject 业务主体
     * @param traceId 调用链 ID
     * @param event 业务事件
     * @param <T> 事件类型
     * @return 完整信封
     */
    public <T extends IntegrationEvent> MessageEnvelope<T> create(
            String source,
            String subject,
            String traceId,
            T event) {
        return new MessageEnvelope<>(
                eventIdSupplier.get(), event.eventType(), event.schemaVersion(), source, subject,
                clock.instant(), traceId, null, Map.of(), event);
    }
}
