package cn.jieyuc.luminova.messaging.reliability;

/**
 * 消费成功记录的持久化端口。
 */
public interface ConsumedEventRepository {

    /**
     * 尝试登记本消费组首次处理事件。
     *
     * @param entity 消费记录
     * @return 首次登记返回 true，唯一键冲突返回 false
     */
    boolean tryInsert(ConsumedEventEntity entity);
}
