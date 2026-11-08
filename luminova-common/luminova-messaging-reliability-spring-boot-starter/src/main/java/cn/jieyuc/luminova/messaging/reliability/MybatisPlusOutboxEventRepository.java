package cn.jieyuc.luminova.messaging.reliability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 基于 MyBatis-Plus 的 Outbox 仓储适配器。
 *
 * <p>多实例通过实体 {@code @Version} 乐观锁竞争租约，更新失败表示其他实例已经领取。</p>
 */
public final class MybatisPlusOutboxEventRepository implements OutboxEventRepository {

    private final OutboxEventMapper mapper;

    public MybatisPlusOutboxEventRepository(OutboxEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void append(OutboxEventEntity event) {
        mapper.insert(event);
    }

    @Override
    public List<OutboxEventEntity> findPublishable(Instant now, int limit) {
        return mapper.selectPage(
                Page.of(1, limit),
                Wrappers.<OutboxEventEntity>lambdaQuery()
                        .and(wrapper -> wrapper
                                .eq(OutboxEventEntity::getStatus, OutboxStatus.PENDING.name())
                                .le(OutboxEventEntity::getNextAttemptAt, now)
                                .or()
                                .eq(OutboxEventEntity::getStatus, OutboxStatus.CLAIMED.name())
                                .lt(OutboxEventEntity::getLeaseUntil, now))
                        .orderByAsc(OutboxEventEntity::getNextAttemptAt))
                .getRecords();
    }

    /**
     * 使用读取时版本更新实体；版本冲突时不覆盖其他实例租约。
     */
    @Override
    public boolean claim(
            OutboxEventEntity event,
            String owner,
            Instant now,
            Duration leaseDuration) {
        event.setStatus(OutboxStatus.CLAIMED.name());
        event.setLeaseOwner(owner);
        event.setLeaseUntil(now.plus(leaseDuration));
        return mapper.updateById(event) == 1;
    }

    @Override
    public void markPublished(OutboxEventEntity event, String owner, Instant now) {
        assertLeaseOwner(event, owner);
        event.setStatus(OutboxStatus.PUBLISHED.name());
        event.setPublishedAt(now);
        event.setLeaseOwner(null);
        event.setLeaseUntil(null);
        if (mapper.updateById(event) != 1) {
            throw new IllegalStateException("Outbox 发布状态更新发生并发冲突");
        }
    }

    @Override
    public void reschedule(
            OutboxEventEntity event,
            String owner,
            Instant nextAttemptAt,
            String errorCode) {
        assertLeaseOwner(event, owner);
        event.setStatus(OutboxStatus.PENDING.name());
        event.setAttempts(event.getAttempts() + 1);
        event.setNextAttemptAt(nextAttemptAt);
        event.setLastErrorCode(errorCode);
        event.setLeaseOwner(null);
        event.setLeaseUntil(null);
        if (mapper.updateById(event) != 1) {
            throw new IllegalStateException("Outbox 重试状态更新发生并发冲突");
        }
    }

    private void assertLeaseOwner(OutboxEventEntity event, String owner) {
        if (!owner.equals(event.getLeaseOwner())) {
            throw new IllegalStateException("当前实例不拥有 Outbox 事件租约");
        }
    }
}
