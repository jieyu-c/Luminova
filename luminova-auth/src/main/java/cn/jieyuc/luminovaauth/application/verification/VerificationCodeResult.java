package cn.jieyuc.luminovaauth.application.verification;

/**
 * 验证码发送受理结果。
 */
public record VerificationCodeResult(String requestId, long expiresInSeconds,
                                     long retryAfterSeconds) {
}
