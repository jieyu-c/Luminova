package cn.jieyuc.luminova.messaging.reliability;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageDestination;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import cn.jieyuc.luminova.messaging.core.JacksonMessageCodec;
import cn.jieyuc.luminova.messaging.core.MapMessageDestinationResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Outbox 必须依赖调用方事务的保护测试。
 */
class TransactionalOutboxEventAppenderTest {

    @Test
    @DisplayName("没有活动事务时拒绝追加Outbox事件")
    void appendWithoutTransactionMustFail() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        TransactionalOutboxEventAppender appender = new TransactionalOutboxEventAppender(
                mock(OutboxEventRepository.class),
                new JacksonMessageCodec(objectMapper),
                new MapMessageDestinationResolver(Map.of("TEST", "test-topic")),
                objectMapper,
                () -> 1L,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
        TestEvent event = new TestEvent("value");
        MessageEnvelope<TestEvent> envelope = new MessageEnvelope<>(
                "event-1", event.eventType(), 1, "test", "1", Instant.now(),
                null, null, Map.of(), event);

        assertThrows(
                OutboxTransactionRequiredException.class,
                () -> appender.append(MessageDestination.normal("TEST"), envelope));
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
}
