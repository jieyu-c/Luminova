package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.MessageHandler;
import cn.jieyuc.luminova.messaging.api.MessageHandlerDescriptor;
import cn.jieyuc.luminova.messaging.core.exception.MessageConfigurationException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动期校验并索引业务 Handler 的注册表。
 */
public final class MessageHandlerRegistry {

    private final Map<HandlerKey, MessageHandler<?>> handlers;

    public MessageHandlerRegistry(Collection<MessageHandler<?>> handlers) {
        Map<HandlerKey, MessageHandler<?>> indexed = new LinkedHashMap<>();
        for (MessageHandler<?> handler : handlers) {
            MessageHandlerDescriptor descriptor = handler.descriptor();
            HandlerKey key = new HandlerKey(
                    descriptor.consumerGroupCode(),
                    descriptor.topicCode(),
                    descriptor.eventType());
            if (indexed.putIfAbsent(key, handler) != null) {
                throw new MessageConfigurationException("消息 Handler 注册冲突: " + key);
            }
        }
        this.handlers = Map.copyOf(indexed);
    }

    /**
     * 查找指定订阅下的事件 Handler。
     *
     * @return 注册的 Handler
     */
    public MessageHandler<?> require(String groupCode, String topicCode, String eventType) {
        HandlerKey key = new HandlerKey(groupCode, topicCode, eventType);
        MessageHandler<?> handler = handlers.get(key);
        if (handler == null) {
            throw new MessageConfigurationException("未注册消息 Handler: " + key);
        }
        return handler;
    }

    /**
     * 返回所有不可变 Handler，供 Adapter 聚合订阅。
     *
     * @return Handler 列表
     */
    public List<MessageHandler<?>> all() {
        return List.copyOf(handlers.values());
    }

    private record HandlerKey(String groupCode, String topicCode, String eventType) {
    }
}
