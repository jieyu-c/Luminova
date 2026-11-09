package cn.jieyuc.luminova.messaging.api;

/**
 * 业务消息处理端口。
 *
 * @param <T> 事件载荷类型
 */
public interface MessageHandler<T extends IntegrationEvent> {

    /**
     * 返回订阅、版本和幂等要求。
     *
     * @return Handler 描述
     */
    MessageHandlerDescriptor descriptor();

    /**
     * 返回反序列化白名单中的载荷类型。
     *
     * @return 载荷类型
     */
    Class<T> payloadType();

    /**
     * 处理事件；失败必须抛出异常以触发回滚和 Broker 重试。
     *
     * @param context 消费上下文
     * @param event 业务事件
     */
    void handle(MessageContext context, T event);
}
