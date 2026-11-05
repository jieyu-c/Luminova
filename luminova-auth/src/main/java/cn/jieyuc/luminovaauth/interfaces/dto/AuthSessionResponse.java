package cn.jieyuc.luminovaauth.interfaces.dto;

/**
 * 注册或登录成功后的会话响应。
 *
 * <p>token 是敏感值，响应对象不得记录日志。</p>
 */
public record AuthSessionResponse(String token, long expiresInSeconds, long userId, String role) {
}
