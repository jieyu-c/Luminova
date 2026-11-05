package cn.jieyuc.user.domain.account;

import java.util.Optional;

/**
 * 用户主体聚合仓储。
 *
 * <p>接口是领域与持久化之间的 seam，不得暴露 MyBatis 类型、持久化 Entity 或查询构造器。</p>
 */
public interface UserAccountRepository {

    /**
     * 按稳定用户 ID 加载完整聚合。
     */
    Optional<UserAccount> findById(UserId userId);

    /**
     * 按登录身份加载所属用户聚合。
     */
    Optional<UserAccount> findByIdentity(IdentityKey identityKey);

    /**
     * 保存完整聚合。
     *
     * <p>实现必须在同一数据库事务内保存主体、身份和凭证，并由唯一约束解决并发注册。</p>
     */
    void save(UserAccount account);
}
