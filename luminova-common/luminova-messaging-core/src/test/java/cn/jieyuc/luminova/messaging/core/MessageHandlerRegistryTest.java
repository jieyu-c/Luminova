package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.ConsumptionMode;
import cn.jieyuc.luminova.messaging.api.IdempotencyMode;
import cn.jieyuc.luminova.messaging.api.IntegrationEvent;
import cn.jieyuc.luminova.messaging.api.MessageContext;
import cn.jieyuc.luminova.messaging.api.MessageHandler;
import cn.jieyuc.luminova.messaging.api.MessageHandlerDescriptor;
import cn.jieyuc.luminova.messaging.core.exception.MessageConfigurationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Handler 注册冲突测试。
 */
class MessageHandlerRegistryTest {

    @Test
    @DisplayName("同消费组Topic和事件类型不能重复注册")
    void duplicateHandlerMustFailAtStartup() {
        assertThrows(
                MessageConfigurationException.class,
                () -> new MessageHandlerRegistry(List.of(new TestHandler(), new TestHandler())));
    }

    private static final class TestHandler implements MessageHandler<TestEvent> {

        @Override
        public MessageHandlerDescriptor descriptor() {
            return new MessageHandlerDescriptor(
                    "TEST_GROUP", "TEST_TOPIC", "test.created", 1, 1,
                    ConsumptionMode.CONCURRENT, IdempotencyMode.DISABLED);
        }

        @Override
        public Class<TestEvent> payloadType() {
            return TestEvent.class;
        }

        @Override
        public void handle(MessageContext context, TestEvent event) {
        }
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
