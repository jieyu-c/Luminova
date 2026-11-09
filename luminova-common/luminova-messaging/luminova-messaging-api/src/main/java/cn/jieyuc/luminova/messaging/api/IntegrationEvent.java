package cn.jieyuc.luminova.messaging.api;

/**
 * 跨上下文集成事件的稳定契约。
 *
 * <p>事件表示已经发生的业务事实，不包含 Topic、消费组或厂商消息对象。</p>
 */
public interface IntegrationEvent {

    /**
     * 返回稳定的事件类型标识。
     *
     * @return 事件类型
     */
    String eventType();

    /**
     * 返回事件载荷的结构版本。
     *
     * @return 大于零的结构版本
     */
    int schemaVersion();
}
