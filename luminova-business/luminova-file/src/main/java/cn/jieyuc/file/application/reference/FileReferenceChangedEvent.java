package cn.jieyuc.file.application.reference;

import cn.jieyuc.luminova.messaging.api.IntegrationEvent;

/** 业务服务在本地事务中发布的文件引用变化事件。 */
public record FileReferenceChangedEvent(long fileId, String businessContext, String businessObjectType,
                                        String businessObjectId, String referenceType, String referenceKey,
                                        Action action) implements IntegrationEvent {
    public static final String EVENT_TYPE = "FileReferenceChanged";

    /** 返回稳定事件类型。 */
    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    /** 返回载荷版本。 */
    @Override
    public int schemaVersion() {
        return 1;
    }

    /** 引用变化动作。 */
    public enum Action {
        ADDED,
        REMOVED
    }
}
