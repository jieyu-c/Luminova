package cn.jieyuc.luminovaauth.application.login;

import cn.jieyuc.luminovaauth.application.port.login.LoginAuthenticator;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.api.account.PasswordAuthenticationResult;
import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.port.account.AccountGateway;
import org.springframework.stereotype.Component;

/**
 * 手机号密码认证策略。
 *
 * <p>负责验证手机号密码凭证并转换为统一认证主体。本类不创建登录会话、不读取密码
 * 哈希，也不处理 HTTP 响应。异常和日志不得包含原始密码或完整手机号。</p>
 */
@Component
public class PhonePasswordAuthenticator implements LoginAuthenticator<PhonePasswordLoginCommand> {
    private final AccountGateway accountGateway;

    public PhonePasswordAuthenticator(AccountGateway accountGateway) {
        this.accountGateway = accountGateway;
    }

    @Override
    public LoginType supports() {
        return LoginType.PHONE_PASSWORD;
    }

    @Override
    public Class<PhonePasswordLoginCommand> commandType() {
        return PhonePasswordLoginCommand.class;
    }

    /**
     * 校验手机号密码并转成统一认证主体。
     *
     * <p>账号不存在、密码错误、账号停用和空快照都表现为同一错误码。本方法不签发会话。</p>
     */
    @Override
    public AuthenticatedPrincipal authenticate(PhonePasswordLoginCommand command) {
        PasswordAuthenticationResult result = accountGateway.authenticatePhonePassword(
                command.phone(), command.password());
        // 空快照不能继续签发会话，并与认证失败使用同一错误码。
        if (!result.authenticated() || result.snapshot() == null) {
            throw new BizException(BizErrorCode.INVALID_CREDENTIALS);
        }
        AccountAuthSnapshot snapshot = result.snapshot();
        return new AuthenticatedPrincipal(snapshot.userId(), snapshot.identityId(),
                snapshot.role(), snapshot.status());
    }
}
