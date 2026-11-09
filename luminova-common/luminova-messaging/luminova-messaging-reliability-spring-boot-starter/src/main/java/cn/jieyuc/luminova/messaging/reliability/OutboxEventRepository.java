package cn.jieyuc.luminova.messaging.reliability;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Outbox 持久化和多实例租约端口。
 */
public interface OutboxEventRepository {

    /**
     * 追加待发布事件。
     *
     * @param event 事件记录
     */
    void append(OutboxEventEntity event);

    /**
     * 查询当前可领取事件。
     *
     * @param now 当前时间
     * @param limit 批量上限
     * @return 候选事件
     */
    List<OutboxEventEntity> findPublishable(Instant now, int limit);

    /**
     * 通过乐观锁竞争事件租约。
     *
     * @param event 候选事件
     * @param owner 实例标识
     * @param now 当前时间
     * @param leaseDuration 租期
     * @return 是否成功领取
     */
    boolean claim(OutboxEventEntity event, String owner, Instant now, Duration leaseDuration);

    /**
     * 将拥有租约的事件标记为已发布。
     */
    void markPublished(OutboxEventEntity event, String owner, Instant now);

    /**
     * 记录失败并释放租约，供后续退避重试。
     */
    void reschedule(
            OutboxEventEntity event,
            String owner,
            Instant nextAttemptAt,
            String errorCode);
}
