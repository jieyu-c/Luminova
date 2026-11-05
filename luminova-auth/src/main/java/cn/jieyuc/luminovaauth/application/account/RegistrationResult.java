package cn.jieyuc.luminovaauth.application.account;

/**
 * 注册成功结果。
 */
public record RegistrationResult(String token, long expiresInSeconds, long userId, String role) {
}
