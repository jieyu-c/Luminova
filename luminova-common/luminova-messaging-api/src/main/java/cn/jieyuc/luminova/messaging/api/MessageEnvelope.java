package cn.jieyuc.luminova.messaging.api;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * 跨消息平台传输的统一事件信封。
 *
 * <p>{@code eventId} 在所有重试中保持不变，是消费幂等的稳定键。</p>
 *
 * @param eventId 全局事件 ID
 * @param eventType 事件类型
 * @param schemaVersion 结构版本
 * @param source 事件来源服务
 * @param subject 关联业务主体
 * @param occurredAt 事实发生时间
 * @param traceId 调用链标识
 * @param orderingKey 局部顺序键
 * @param attributes 非敏感扩展属性
 * @param payload 事件载荷
 * @param <T> 事件类型
 */
public record MessageEnvelope<T extends IntegrationEvent>(
        String eventId,
        String eventType,
        int schemaVersion,
        String source,
        String subject,
        Instant occurredAt,
        String traceId,
        String orderingKey,
        Map<String, String> attributes,
        T payload) {

    /**
     * 校验信封与载荷契约一致，并冻结扩展属性。
     */
    public MessageEnvelope {
        requireText(eventId, "事件 ID");
        requireText(eventType, "事件类型");
        requireText(source, "事件来源");
        Objects.requireNonNull(occurredAt, "事件发生时间不能为空");
        Objects.requireNonNull(payload, "事件载荷不能为空");
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("结构版本必须大于零");
        }
        if (!eventType.equals(payload.eventType()) || schemaVersion != payload.schemaVersion()) {
            throw new IllegalArgumentException("信封契约必须与事件载荷一致");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
    }
}
