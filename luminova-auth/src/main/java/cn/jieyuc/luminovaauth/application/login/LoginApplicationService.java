package cn.jieyuc.luminovaauth.application.login;

import cn.jieyuc.luminovaauth.application.port.login.LoginRateLimiter;

import cn.jieyuc.luminova.api.account.AccountStatusCode;
import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.session.IssuedSession;
import cn.jieyuc.luminovaauth.application.port.session.SessionIssuer;
import cn.jieyuc.luminovaauth.application.port.security.SecurityAuditRecorder;
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
     * <p>顺序固定为限流、认证、账号状态、签发会话。停用账号与凭证错误使用同一错误码。
     * 只有进入认证后失败才累计该身份的连续失败次数，方法开头的限流不计入。</p>
     *
     * @param command 敏感登录命令，禁止整体记录日志
     * @return 会话与最小用户信息
     * @throws BizException 凭证无效、账号不可用或触发限流时抛出
     */
    public LoginResult login(PhonePasswordLoginCommand command) {
        rateLimiter.checkAttempt(command.loginType(), command.phone(), command.clientIp());
        try {
            AuthenticatedPrincipal principal = registry.authenticate(command);
            // 停用与密码错误对外相同，避免调用方判断账号是否存在或已被禁用。
            if (!AccountStatusCode.ACTIVE.matches(principal.status())) {
                throw new BizException(BizErrorCode.INVALID_CREDENTIALS);
            }
            rateLimiter.onSuccess(command.loginType(), command.phone());
            IssuedSession session = sessionIssuer.issue(principal);
            auditRecorder.loginSucceeded(principal.userId(), maskPhone(command.phone()),
                    command.clientIp(), command.userAgent(), command.requestId());
            return new LoginResult(session.token(), session.expiresInSeconds(),
                    principal.userId(), principal.role());
        } catch (BizException exception) {
            // 认证阶段的失败才锁定身份；前面的限流不会进入这里。
            rateLimiter.onFailure(command.loginType(), command.phone());
            auditRecorder.loginFailed(maskPhone(command.phone()), command.clientIp(),
                    command.userAgent(), command.requestId());
            throw exception;
        }
    }

    /** 生成审计用的脱敏手机号。无法识别的输入写成固定占位，避免原值进入审计。 */
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
