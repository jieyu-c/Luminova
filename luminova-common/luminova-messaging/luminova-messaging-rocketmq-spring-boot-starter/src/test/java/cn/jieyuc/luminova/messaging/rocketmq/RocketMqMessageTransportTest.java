package cn.jieyuc.luminova.messaging.rocketmq;

import cn.jieyuc.luminova.messaging.api.MessageMode;
import cn.jieyuc.luminova.messaging.api.MessagePublishResult;
import cn.jieyuc.luminova.messaging.core.EncodedMessage;
import cn.jieyuc.luminova.messaging.core.ResolvedDestination;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.message.MessageId;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RocketMQ 协议映射测试。
 */
class RocketMqMessageTransportTest {

    @Test
    @DisplayName("FIFO消息映射Topic Key Tag和MessageGroup")
    void fifoMessageIsMappedToRocketMq() throws Exception {
        ClientServiceProvider provider = mock(ClientServiceProvider.class);
        Producer producer = mock(Producer.class);
        MessageBuilder builder = mock(MessageBuilder.class);
        Message rocketMessage = mock(Message.class);
        SendReceipt receipt = mock(SendReceipt.class);
        MessageId messageId = mock(MessageId.class);
        when(provider.newMessageBuilder()).thenReturn(builder);
        when(builder.setTopic("physical-topic")).thenReturn(builder);
        when(builder.setBody(org.mockito.ArgumentMatchers.any())).thenReturn(builder);
        when(builder.setTag("canvas.deleted")).thenReturn(builder);
        when(builder.setKeys("event-1")).thenReturn(builder);
        when(builder.addProperty("x-event-id", "event-1")).thenReturn(builder);
        when(builder.setMessageGroup("canvas-1")).thenReturn(builder);
        when(builder.build()).thenReturn(rocketMessage);
        when(producer.send(rocketMessage)).thenReturn(receipt);
        when(receipt.getMessageId()).thenReturn(messageId);
        when(messageId.toString()).thenReturn("provider-1");
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        RocketMqMessageTransport transport = new RocketMqMessageTransport(provider, producer, clock);

        MessagePublishResult result = transport.publish(
                new ResolvedDestination("CANVAS", "physical-topic", MessageMode.FIFO, null),
                new EncodedMessage(
                        "event-1", "canvas.deleted", "canvas-1", new byte[]{1},
                        Map.of("x-event-id", "event-1")));

        verify(builder).setMessageGroup("canvas-1");
        assertEquals("provider-1", result.providerMessageId());
    }
}
