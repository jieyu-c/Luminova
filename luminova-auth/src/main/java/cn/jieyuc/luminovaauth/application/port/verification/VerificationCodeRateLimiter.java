package cn.jieyuc.luminovaauth.application.port.verification;

/**
 * 验证码发送限流端口。
 */
public interface VerificationCodeRateLimiter {

    /**
     * 检查并记录一次验证码发送请求。
     *
     * @throws cn.jieyuc.luminova.base.exceptions.BizException 超过手机号或 IP 限制时抛出
     */
    void checkAndRecord(String normalizedPhone, String clientIp);
}
