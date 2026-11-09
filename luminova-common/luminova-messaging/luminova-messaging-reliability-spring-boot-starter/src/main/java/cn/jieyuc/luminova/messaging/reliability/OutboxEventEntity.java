package cn.jieyuc.luminova.messaging.reliability;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * SDK Outbox 持久化记录。
 *
 * <p>只保存平台无关的逻辑目的地与编码信封；厂商回执不是业务事实。</p>
 */
@TableName("mq_outbox_event")
@Getter
@Setter
public class OutboxEventEntity {

    @TableId(type = IdType.INPUT)
    private Long id;
    private String eventId;
    private String topicCode;
    private String messageMode;
    private String eventType;
    private Integer schemaVersion;
    private String orderingKey;
    private Instant deliverAt;
    private String envelopeJson;
    private String propertiesJson;
    private String status;
    private Integer attempts;
    private Instant nextAttemptAt;
    private String leaseOwner;
    private Instant leaseUntil;
    private String lastErrorCode;
    private Instant createdAt;
    private Instant publishedAt;
    @Version
    private Integer version;

}
