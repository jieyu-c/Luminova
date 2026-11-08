package cn.jieyuc.luminova.messaging.reliability;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 消费成功记录；由消费组和事件 ID 共同构成业务幂等键。
 */
@TableName("mq_consumed_event")
@Getter
@Setter
public class ConsumedEventEntity {

    private String consumerGroup;
    private String eventId;
    private String eventType;
    private String payloadDigest;
    private Instant consumedAt;

}
