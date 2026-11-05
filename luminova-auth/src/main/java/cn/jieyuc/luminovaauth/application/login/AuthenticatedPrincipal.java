package cn.jieyuc.luminovaauth.application.login;

/**
 * 所有登录策略统一产出的认证主体。
 *
 * <p>登录会话始终绑定稳定 userId，不绑定手机号等可变登录身份。</p>
 */
public record AuthenticatedPrincipal(long userId, long identityId, String role, String status) {
}
