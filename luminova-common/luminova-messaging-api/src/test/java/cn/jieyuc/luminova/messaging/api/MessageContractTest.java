package cn.jieyuc.luminova.messaging.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 公共消息契约不变量测试。
 */
class MessageContractTest {

    @Test
    @DisplayName("延迟消息必须提供投递时间")
    void delayedMessageRequiresDeliveryTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MessageDestination("FILE_EVENT", MessageMode.DELAY, null));
    }

    @Test
    @DisplayName("信封契约必须与载荷声明一致")
    void envelopeMustMatchPayloadContract() {
        TestEvent event = new TestEvent("value");
        assertThrows(
                IllegalArgumentException.class,
                () -> new MessageEnvelope<>(
                        "event-1", "wrong.type", 1, "test", "1", Instant.now(),
                        null, null, Map.of(), event));
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
