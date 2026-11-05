package cn.jieyuc.luminovaauth.infrastructure.rpc;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.api.account.AccountAuthenticationApi;
import cn.jieyuc.luminova.api.account.PasswordAuthenticationResult;
import cn.jieyuc.luminova.api.account.PhonePasswordQuery;
import cn.jieyuc.luminova.api.account.RegisterPhoneUserCommand;
import cn.jieyuc.luminova.api.account.ResetPhonePasswordCommand;
import cn.jieyuc.luminova.api.account.SecurityAuditCommand;
import cn.jieyuc.luminovaauth.application.account.AccountGateway;
import cn.jieyuc.luminovaauth.application.security.SecurityAuditRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 账号上下文 Dubbo Gateway Adapter。
 *
 * <p>负责跨上下文 DTO 转换和远程调用，不缓存或记录原始密码。远程超时由 Dubbo 公共
 * 配置控制，失败时不得降级为认证成功。</p>
 */
@Component
public class DubboAccountGateway implements AccountGateway, SecurityAuditRecorder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboAccountGateway.class);
    @DubboReference(check = false)
    private AccountAuthenticationApi accountApi;

    @Override
    public AccountAuthSnapshot registerPhoneUser(String phone, String password) {
        return accountApi.registerPhoneUser(new RegisterPhoneUserCommand(phone, password));
    }

    @Override
    public PasswordAuthenticationResult authenticatePhonePassword(String phone, String password) {
        return accountApi.authenticatePhonePassword(new PhonePasswordQuery(phone, password));
    }

    @Override
    public AccountAuthSnapshot resetPhonePassword(String phone, String newPassword) {
        return accountApi.resetPhonePassword(new ResetPhonePasswordCommand(phone, newPassword));
    }

    @Override
    public void loginSucceeded(long userId, String maskedIdentifier, String ip,
                               String userAgent, String requestId) {
        recordSafely(new SecurityAuditCommand("LOGIN_SUCCESS", userId, userId, "PHONE",
                maskedIdentifier, "SUCCESS", null, ip, userAgent, requestId));
    }

    @Override
    public void loginFailed(String maskedIdentifier, String ip, String userAgent, String requestId) {
        recordSafely(new SecurityAuditCommand("LOGIN_FAILURE", null, null, "PHONE",
                maskedIdentifier, "FAILURE", "INVALID_CREDENTIALS", ip, userAgent, requestId));
    }

    private void recordSafely(SecurityAuditCommand command) {
        try {
            accountApi.recordSecurityAudit(command);
        } catch (RuntimeException exception) {
            // 审计故障不能改变认证结果；只记录不含身份原值的固定告警并交由监控处理。
            LOGGER.warn("安全审计事件写入失败，requestId={}", command.requestId());
        }
    }
}
