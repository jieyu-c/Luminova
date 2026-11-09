package cn.jieyuc.luminova.messaging.reliability;

import org.springframework.dao.DuplicateKeyException;

/**
 * 基于数据库唯一键实现幂等判定的消费记录适配器。
 */
public final class MybatisPlusConsumedEventRepository implements ConsumedEventRepository {

    private final ConsumedEventMapper mapper;

    public MybatisPlusConsumedEventRepository(ConsumedEventMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 只有唯一键冲突可以视为已消费，其他数据库异常必须继续抛出以触发重试。
     */
    @Override
    public boolean tryInsert(ConsumedEventEntity entity) {
        try {
            mapper.insert(entity);
            return true;
        } catch (DuplicateKeyException exception) {
            return false;
        }
    }
}
