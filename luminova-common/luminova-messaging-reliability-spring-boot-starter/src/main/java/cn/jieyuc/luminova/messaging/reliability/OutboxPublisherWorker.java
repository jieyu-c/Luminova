package cn.jieyuc.luminova.messaging.reliability;

import cn.jieyuc.luminova.messaging.api.MessageMode;
import cn.jieyuc.luminova.messaging.api.MessageDestination;
import cn.jieyuc.luminova.messaging.core.EncodedMessage;
import cn.jieyuc.luminova.messaging.core.MessageTransport;
import cn.jieyuc.luminova.messaging.core.MessageDestinationResolver;
import cn.jieyuc.luminova.messaging.core.ResolvedDestination;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 领取并发布本服务 Outbox 事件的恢复型 Worker。
 *
 * <p>Broker 已接收但状态更新前宕机会产生重复投递，消费者必须按 eventId 幂等。</p>
 */
public final class OutboxPublisherWorker {

    private static final String PUBLISH_ERROR = "PUBLISH_FAILED";

    private final OutboxEventRepository repository;
    private final MessageTransport transport;
    private final MessageDestinationResolver destinationResolver;
    private final ObjectMapper objectMapper;
    private final OutboxRetryPolicy retryPolicy;
    private final Clock clock;
    private final String instanceId;
    private final int batchSize;
    private final Duration leaseDuration;

    public OutboxPublisherWorker(
            OutboxEventRepository repository,
            MessageTransport transport,
            MessageDestinationResolver destinationResolver,
            ObjectMapper objectMapper,
            OutboxRetryPolicy retryPolicy,
            Clock clock,
            String instanceId,
            int batchSize,
            Duration leaseDuration) {
        this.repository = repository;
        this.transport = transport;
        this.destinationResolver = destinationResolver;
        this.objectMapper = objectMapper;
        this.retryPolicy = retryPolicy;
        this.clock = clock;
        this.instanceId = instanceId;
        this.batchSize = batchSize;
        this.leaseDuration = leaseDuration;
    }

    /**
     * 发布一批到期事件；单条失败不会阻断同批其他事件。
     *
     * @return 成功发布数量
     */
    public int publishBatch() {
        List<OutboxEventEntity> events = repository.findPublishable(clock.instant(), batchSize);
        int published = 0;
        for (OutboxEventEntity event : events) {
            if (publishOne(event)) {
                published++;
            }
        }
        return published;
    }

    /**
     * 先竞争租约再发布；租约失败表示其他实例已经处理，不属于发布失败。
     */
    private boolean publishOne(OutboxEventEntity event) {
        if (!repository.claim(event, instanceId, clock.instant(), leaseDuration)) {
            return false;
        }
        try {
            transport.publish(toDestination(event), toMessage(event));
            repository.markPublished(event, instanceId, clock.instant());
            return true;
        } catch (RuntimeException exception) {
            int attempts = event.getAttempts() + 1;
            repository.reschedule(
                    event,
                    instanceId,
                    retryPolicy.nextRetryAt(attempts, clock.instant()),
                    PUBLISH_ERROR);
            return false;
        }
    }

    private ResolvedDestination toDestination(OutboxEventEntity event) {
        return destinationResolver.resolve(new MessageDestination(
                event.getTopicCode(),
                MessageMode.valueOf(event.getMessageMode()),
                event.getDeliverAt()));
    }

    private EncodedMessage toMessage(OutboxEventEntity event) {
        try {
            Map<String, String> properties = objectMapper.readValue(
                    event.getPropertiesJson(), new TypeReference<>() { });
            return new EncodedMessage(
                    event.getEventId(),
                    event.getEventType(),
                    event.getOrderingKey(),
                    event.getEnvelopeJson().getBytes(StandardCharsets.UTF_8),
                    properties);
        } catch (Exception exception) {
            throw new IllegalStateException("Outbox 消息还原失败，eventId=" + event.getEventId(), exception);
        }
    }
}
