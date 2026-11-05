package cn.jieyuc.luminova.api.account;

/**
 * 账号上下文对认证上下文提供的防腐层接口。
 *
 * <p>接口只暴露认证所需的稳定结果，不返回领域聚合、持久化实体或密码哈希。
 * 原始密码属于敏感凭证，调用方和实现方均不得记录请求对象。</p>
 */
public interface AccountAuthenticationApi {

    /**
     * 创建手机号密码用户。
     *
     * @param command 已完成手机号所有权验证的注册命令
     * @return 新建用户的认证快照
     */
    AccountAuthSnapshot registerPhoneUser(RegisterPhoneUserCommand command);

    /**
     * 验证手机号密码，并始终返回统一认证结果。
     *
     * <p>手机号不存在时实现方仍执行假哈希校验，以降低通过响应耗时枚举账号的风险。</p>
     *
     * @param query 手机号密码认证请求
     * @return 认证结果；失败时不区分手机号不存在、密码错误或账号不可用
     */
    PasswordAuthenticationResult authenticatePhonePassword(PhonePasswordQuery query);

    /**
     * 使用已验证手机号重置密码。
     *
     * @param command 重置密码命令
     */
    AccountAuthSnapshot resetPhonePassword(ResetPhonePasswordCommand command);

    /**
     * 查询用户当前角色和账号状态。
     *
     * @param userId 用户主体 ID
     * @return 认证快照
     */
    AccountAuthSnapshot getAuthSnapshot(long userId);

    /**
     * 记录不包含敏感原值的安全审计事件。
     */
    void recordSecurityAudit(SecurityAuditCommand command);
}
