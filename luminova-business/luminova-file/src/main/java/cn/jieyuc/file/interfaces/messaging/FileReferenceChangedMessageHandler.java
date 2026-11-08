package cn.jieyuc.file.interfaces.messaging;

import cn.jieyuc.file.application.reference.ChangeFileReferenceApplicationService;
import cn.jieyuc.file.application.reference.FileReferenceChangedEvent;
import cn.jieyuc.luminova.messaging.api.ConsumptionMode;
import cn.jieyuc.luminova.messaging.api.IdempotencyMode;
import cn.jieyuc.luminova.messaging.api.MessageContext;
import cn.jieyuc.luminova.messaging.api.MessageHandler;
import cn.jieyuc.luminova.messaging.api.MessageHandlerDescriptor;
import org.springframework.stereotype.Component;

/** 消费业务模块发布的文件引用变化事件并维护本地引用账本。 */
@Component
public class FileReferenceChangedMessageHandler implements MessageHandler<FileReferenceChangedEvent> {
    private static final MessageHandlerDescriptor DESCRIPTOR = new MessageHandlerDescriptor(
            "file-reference-ledger", "business-file-reference", FileReferenceChangedEvent.EVENT_TYPE,
            1, 1, ConsumptionMode.CONCURRENT, IdempotencyMode.REQUIRED);
    private final ChangeFileReferenceApplicationService service;

    public FileReferenceChangedMessageHandler(ChangeFileReferenceApplicationService service) {
        this.service = service;
    }

    /** 返回引用账本消费契约，并要求 SDK 本地事务幂等。 */
    @Override
    public MessageHandlerDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** 返回反序列化白名单载荷类型。 */
    @Override
    public Class<FileReferenceChangedEvent> payloadType() {
        return FileReferenceChangedEvent.class;
    }

    /** 将可信事件交给应用服务处理，失败抛出以触发 Broker 重试。 */
    @Override
    public void handle(MessageContext context, FileReferenceChangedEvent event) {
        service.change(event);
    }
}
