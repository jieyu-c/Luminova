package cn.jieyuc.luminovaauth.application.login;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.session.IssuedSession;
import cn.jieyuc.luminovaauth.application.session.SessionIssuer;
import cn.jieyuc.luminovaauth.application.security.SecurityAuditRecorder;
import org.springframework.stereotype.Service;

/**
 * 统一登录应用服务。
 *
 * <p>固定执行限流、策略认证、账号状态检查和会话签发。具体策略不得绕过该管线直接创建
 * Token，从而保证未来新增登录方式仍执行相同安全规则。</p>
 */
@Service
public class LoginApplicationService {
    private final LoginAuthenticatorRegistry registry;
    private final LoginRateLimiter rateLimiter;
    private final SessionIssuer sessionIssuer;
    private final SecurityAuditRecorder auditRecorder;

    public LoginApplicationService(LoginAuthenticatorRegistry registry,
                                   LoginRateLimiter rateLimiter,
                                   SessionIssuer sessionIssuer,
                                   SecurityAuditRecorder auditRecorder) {
        this.registry = registry;
        this.rateLimiter = rateLimiter;
        this.sessionIssuer = sessionIssuer;
        this.auditRecorder = auditRecorder;
    }

    /**
     * 执行手机号密码登录。
     *
     * @param command 敏感登录命令，禁止整体记录日志
     * @return 会话与最小用户信息
     */
    public LoginResult login(PhonePasswordLoginCommand command) {
        rateLimiter.checkAttempt(command.loginType(), command.phone(), command.clientIp());
        try {
            AuthenticatedPrincipal principal = registry.authenticate(command);
            if (!"ACTIVE".equals(principal.status())) {
                throw new BizException("INVALID_CREDENTIALS", "手机号或密码错误");
            }
            rateLimiter.onSuccess(command.loginType(), command.phone());
            IssuedSession session = sessionIssuer.issue(principal);
            auditRecorder.loginSucceeded(principal.userId(), maskPhone(command.phone()),
                    command.clientIp(), command.userAgent(), command.requestId());
            return new LoginResult(session.token(), session.expiresInSeconds(),
                    principal.userId(), principal.role());
        } catch (BizException exception) {
            rateLimiter.onFailure(command.loginType(), command.phone());
            auditRecorder.loginFailed(maskPhone(command.phone()), command.clientIp(),
                    command.userAgent(), command.requestId());
            throw exception;
        }
    }

    private String maskPhone(String value) {
        if (value == null) {
            return "unknown";
        }
        String local = value.replace("+86", "").replaceAll("\\D", "");
        return local.length() == 11
                ? "+86" + local.substring(0, 3) + "****" + local.substring(7)
                : "invalid";
    }
}
