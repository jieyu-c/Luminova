package cn.jieyuc.luminovaauth.application.session;

/**
 * 会话签发结果。
 */
public record IssuedSession(String token, long expiresInSeconds) {
}
