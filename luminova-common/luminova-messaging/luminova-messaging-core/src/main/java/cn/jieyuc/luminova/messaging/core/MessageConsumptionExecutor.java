package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import cn.jieyuc.luminova.messaging.api.MessageHandlerDescriptor;

/**
 * 包装 Handler 副作用的消费执行策略。
 *
 * <p>可靠性 Starter 可替换该端口，将消费记录和业务操作放入同一本地事务。</p>
 */
public interface MessageConsumptionExecutor {

    /**
     * 执行一次消息处理。
     *
     * @param descriptor Handler 描述
     * @param envelope 消息信封
     * @param payloadDigest 正文摘要
     * @param handler 业务处理逻辑
     */
    void execute(
            MessageHandlerDescriptor descriptor,
            MessageEnvelope<?> envelope,
            String payloadDigest,
            Runnable handler);
}
