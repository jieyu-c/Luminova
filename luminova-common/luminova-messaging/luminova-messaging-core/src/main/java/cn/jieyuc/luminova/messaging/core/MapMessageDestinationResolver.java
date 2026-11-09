package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.MessageDestination;
import cn.jieyuc.luminova.messaging.core.exception.MessageConfigurationException;

import java.util.Map;

/**
 * 基于启动配置的逻辑 Topic 解析器。
 */
public final class MapMessageDestinationResolver implements MessageDestinationResolver {

    private final Map<String, String> topicMappings;

    public MapMessageDestinationResolver(Map<String, String> topicMappings) {
        this.topicMappings = Map.copyOf(topicMappings);
    }

    /**
     * 将逻辑 Topic 转换为物理 Topic。
     *
     * @param destination 逻辑目的地
     * @return 物理目的地
     */
    @Override
    public ResolvedDestination resolve(MessageDestination destination) {
        String physicalTopic = topicMappings.get(destination.topicCode());
        if (physicalTopic == null || physicalTopic.isBlank()) {
            throw new MessageConfigurationException("逻辑 Topic 未配置物理映射: " + destination.topicCode());
        }
        return new ResolvedDestination(
                destination.topicCode(),
                physicalTopic,
                destination.mode(),
                destination.deliverAt());
    }
}
