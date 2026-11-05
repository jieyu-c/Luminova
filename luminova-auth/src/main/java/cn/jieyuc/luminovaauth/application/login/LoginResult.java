package cn.jieyuc.luminovaauth.application.login;

/**
 * 统一登录用例结果。
 */
public record LoginResult(String token, long expiresInSeconds, long userId, String role) {
}
