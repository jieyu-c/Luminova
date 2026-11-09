package cn.jieyuc.luminova.messaging.reliability;

import cn.jieyuc.luminova.messaging.api.MessagePublishResult;
import cn.jieyuc.luminova.messaging.core.EncodedMessage;
import cn.jieyuc.luminova.messaging.core.MessageTransport;
import cn.jieyuc.luminova.messaging.core.MapMessageDestinationResolver;
import cn.jieyuc.luminova.messaging.core.ResolvedDestination;
import cn.jieyuc.luminova.messaging.core.TransportCapabilities;
import cn.jieyuc.luminova.messaging.core.TransportProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Outbox 发布恢复流程测试。
 */
class OutboxPublisherWorkerTest {

    @Test
    @DisplayName("领取成功并收到Broker确认后标记为已发布")
    void claimedEventIsMarkedPublished() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        FakeRepository repository = new FakeRepository(event(now));
        OutboxPublisherWorker worker = new OutboxPublisherWorker(
                repository,
                new SuccessfulTransport(now),
                new MapMessageDestinationResolver(java.util.Map.of("TEST", "test-topic")),
                new ObjectMapper(),
                new ExponentialOutboxRetryPolicy(Duration.ofSeconds(1), Duration.ofMinutes(1)),
                Clock.fixed(now, ZoneOffset.UTC),
                "instance-1",
                10,
                Duration.ofSeconds(30));

        assertEquals(1, worker.publishBatch());
        assertEquals(OutboxStatus.PUBLISHED.name(), repository.event.getStatus());
    }

    private OutboxEventEntity event(Instant now) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(1L);
        event.setEventId("event-1");
        event.setTopicCode("TEST");
        event.setMessageMode("NORMAL");
        event.setEventType("test.created");
        event.setEnvelopeJson("{}");
        event.setPropertiesJson("{}");
        event.setStatus(OutboxStatus.PENDING.name());
        event.setAttempts(0);
        event.setNextAttemptAt(now);
        event.setVersion(0);
        return event;
    }

    private static final class FakeRepository implements OutboxEventRepository {

        private final OutboxEventEntity event;

        private FakeRepository(OutboxEventEntity event) {
            this.event = event;
        }

        @Override
        public void append(OutboxEventEntity event) {
        }

        @Override
        public List<OutboxEventEntity> findPublishable(Instant now, int limit) {
            return List.of(event);
        }

        @Override
        public boolean claim(
                OutboxEventEntity event,
                String owner,
                Instant now,
                Duration leaseDuration) {
            event.setLeaseOwner(owner);
            event.setStatus(OutboxStatus.CLAIMED.name());
            return true;
        }

        @Override
        public void markPublished(OutboxEventEntity event, String owner, Instant now) {
            event.setStatus(OutboxStatus.PUBLISHED.name());
        }

        @Override
        public void reschedule(
                OutboxEventEntity event,
                String owner,
                Instant nextAttemptAt,
                String errorCode) {
            event.setStatus(OutboxStatus.PENDING.name());
        }
    }

    private static final class SuccessfulTransport implements MessageTransport {

        private final Instant now;

        private SuccessfulTransport(Instant now) {
            this.now = now;
        }

        @Override
        public TransportProvider provider() {
            return TransportProvider.ROCKETMQ;
        }

        @Override
        public TransportCapabilities capabilities() {
            return new TransportCapabilities(true, true, true, true, false);
        }

        @Override
        public MessagePublishResult publish(ResolvedDestination destination, EncodedMessage message) {
            return new MessagePublishResult(message.eventId(), "FAKE", "provider-1", now);
        }

        @Override
        public CompletableFuture<MessagePublishResult> publishAsync(
                ResolvedDestination destination,
                EncodedMessage message) {
            return CompletableFuture.completedFuture(publish(destination, message));
        }

        @Override
        public void close() {
        }
    }
}
