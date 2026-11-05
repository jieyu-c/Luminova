package cn.jieyuc.luminovaauth.application.login;

/**
 * 登录尝试限流端口。
 */
public interface LoginRateLimiter {

    /**
     * 在认证前检查并记录一次尝试。
     *
     * @throws cn.jieyuc.luminova.base.exceptions.BizException 超出限制时抛出 RATE_LIMITED
     */
    void checkAttempt(LoginType loginType, String identifier, String clientIp);

    /** 成功认证后清除该身份的连续失败状态。 */
    void onSuccess(LoginType loginType, String identifier);

    /** 认证失败后记录该身份失败次数。 */
    void onFailure(LoginType loginType, String identifier);
}
