package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageDestination;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import cn.jieyuc.luminova.messaging.api.MessagePublishResult;
import cn.jieyuc.luminova.messaging.core.exception.UnsupportedTransportCapabilityException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 默认发布器的平台无关行为测试。
 */
class DefaultMessagePublisherTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    @DisplayName("普通消息使用逻辑映射后交给Transport")
    void normalMessageIsResolvedAndPublished() {
        FakeTransport transport = new FakeTransport();
        DefaultMessagePublisher publisher = publisher(transport);

        MessagePublishResult result = publisher.publish(
                MessageDestination.normal("TEST_EVENT"), envelope(null));

        assertEquals("physical-test-event", transport.destination.physicalTopic());
        assertEquals("event-1", result.eventId());
        assertEquals("event-1", transport.message.properties().get(MessageHeaders.EVENT_ID));
    }

    @Test
    @DisplayName("Transport不支持FIFO时必须明确拒绝")
    void unsupportedFifoMustFail() {
        DefaultMessagePublisher publisher = publisher(new FakeTransport());

        assertThrows(
                UnsupportedTransportCapabilityException.class,
                () -> publisher.publish(MessageDestination.fifo("TEST_EVENT"), envelope("aggregate-1")));
    }

    private DefaultMessagePublisher publisher(MessageTransport transport) {
        return new DefaultMessagePublisher(
                transport,
                new JacksonMessageCodec(new ObjectMapper().findAndRegisterModules()),
                new MapMessageDestinationResolver(Map.of("TEST_EVENT", "physical-test-event")),
                1024 * 1024);
    }

    private MessageEnvelope<TestEvent> envelope(String orderingKey) {
        TestEvent event = new TestEvent("value");
        return new MessageEnvelope<>(
                "event-1", event.eventType(), event.schemaVersion(), "test-service", "1",
                NOW, "trace-1", orderingKey, Map.of(), event);
    }

    private record TestEvent(String value) implements IntegrationEvent {

        @Override
        public String eventType() {
            return "test.created";
        }

        @Override
        public int schemaVersion() {
            return 1;
        }
    }

    private static final class FakeTransport implements MessageTransport {

        private ResolvedDestination destination;
        private EncodedMessage message;

        @Override
        public TransportProvider provider() {
            return TransportProvider.ROCKETMQ;
        }

        @Override
        public TransportCapabilities capabilities() {
            return new TransportCapabilities(false, false, false, false, false);
        }

        @Override
        public MessagePublishResult publish(ResolvedDestination destination, EncodedMessage message) {
            this.destination = destination;
            this.message = message;
            return new MessagePublishResult(message.eventId(), "FAKE", "provider-1", NOW);
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
