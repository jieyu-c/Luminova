package cn.jieyuc.user.application.port.account;

/**
 * 用户主体和登录身份 ID 生成端口。
 *
 * <p>应用层只要求生成全局唯一正整数，不依赖具体雪花算法或数据库实现。</p>
 */
public interface UserIdentifierGenerator {

    /**
     * 生成新的全局唯一正整数 ID。
     */
    long nextId();
}
