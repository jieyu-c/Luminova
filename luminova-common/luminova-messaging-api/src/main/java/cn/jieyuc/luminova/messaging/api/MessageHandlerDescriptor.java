package cn.jieyuc.luminova.messaging.api;

import java.util.Objects;

/**
 * 消息 Handler 的稳定订阅描述。
 *
 * @param consumerGroupCode 逻辑消费组
 * @param topicCode 逻辑 Topic
 * @param eventType 事件类型
 * @param minimumVersion 最低兼容版本
 * @param maximumVersion 最高兼容版本
 * @param consumptionMode 消费模式
 * @param idempotencyMode 幂等要求
 */
public record MessageHandlerDescriptor(
        String consumerGroupCode,
        String topicCode,
        String eventType,
        int minimumVersion,
        int maximumVersion,
        ConsumptionMode consumptionMode,
        IdempotencyMode idempotencyMode) {

    /**
     * 校验订阅描述完整且版本区间有效。
     */
    public MessageHandlerDescriptor {
        requireText(consumerGroupCode, "消费组");
        requireText(topicCode, "逻辑 Topic");
        requireText(eventType, "事件类型");
        Objects.requireNonNull(consumptionMode, "消费模式不能为空");
        Objects.requireNonNull(idempotencyMode, "幂等模式不能为空");
        if (minimumVersion < 1 || maximumVersion < minimumVersion) {
            throw new IllegalArgumentException("事件版本区间无效");
        }
    }

    /**
     * 判断结构版本是否在 Handler 支持范围内。
     *
     * @param schemaVersion 待判断版本
     * @return 是否支持
     */
    public boolean supports(int schemaVersion) {
        return schemaVersion >= minimumVersion && schemaVersion <= maximumVersion;
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
    }
}
