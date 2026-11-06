package cn.jieyuc.luminovaauth.application.port.security;

/**
 * 认证安全审计端口。
 *
 * <p>事件只接收脱敏标识。审计系统故障不得把成功认证改为失败，也不得泄漏凭证。</p>
 */
public interface SecurityAuditRecorder {

    /** 记录登录成功。 */
    void loginSucceeded(long userId, String maskedIdentifier, String ip,
                        String userAgent, String requestId);

    /** 记录不区分内部原因的登录失败。 */
    void loginFailed(String maskedIdentifier, String ip, String userAgent, String requestId);
}
