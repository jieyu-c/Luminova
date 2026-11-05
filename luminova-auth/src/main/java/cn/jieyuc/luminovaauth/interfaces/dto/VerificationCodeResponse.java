package cn.jieyuc.luminovaauth.interfaces.dto;

/**
 * 验证码发送受理响应，不包含明文验证码。
 */
public record VerificationCodeResponse(String requestId, long expiresInSeconds,
                                       long retryAfterSeconds) {
}
