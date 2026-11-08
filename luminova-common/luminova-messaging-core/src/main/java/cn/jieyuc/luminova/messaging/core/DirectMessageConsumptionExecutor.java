package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.IdempotencyMode;
import cn.jieyuc.luminova.messaging.api.MessageEnvelope;
import cn.jieyuc.luminova.messaging.api.MessageHandlerDescriptor;
import cn.jieyuc.luminova.messaging.core.exception.MessageConfigurationException;

/**
 * 未启用可靠性模块时的直接消费策略。
 */
public final class DirectMessageConsumptionExecutor implements MessageConsumptionExecutor {

    /**
     * 仅允许显式关闭幂等要求的 Handler 直接执行。
     */
    @Override
    public void execute(
            MessageHandlerDescriptor descriptor,
            MessageEnvelope<?> envelope,
            String payloadDigest,
            Runnable handler) {
        if (descriptor.idempotencyMode() == IdempotencyMode.REQUIRED) {
            throw new MessageConfigurationException("Handler 要求幂等消费，但未启用可靠性模块");
        }
        handler.run();
    }
}
