package cn.jieyuc.luminova.messaging.core;

import cn.jieyuc.luminova.messaging.api.MessageDestination;

/**
 * 将代码中的逻辑目的地解析为环境物理资源。
 */
public interface MessageDestinationResolver {

    /**
     * 解析逻辑目的地；缺少映射时必须失败而不是静默创建资源。
     *
     * @param destination 逻辑目的地
     * @return 物理目的地
     */
    ResolvedDestination resolve(MessageDestination destination);
}
