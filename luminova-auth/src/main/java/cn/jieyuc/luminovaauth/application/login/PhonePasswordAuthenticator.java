package cn.jieyuc.luminovaauth.application.login;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.api.account.PasswordAuthenticationResult;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.account.AccountGateway;
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

    @Override
    public AuthenticatedPrincipal authenticate(PhonePasswordLoginCommand command) {
        PasswordAuthenticationResult result = accountGateway.authenticatePhonePassword(
                command.phone(), command.password());
        if (!result.authenticated() || result.snapshot() == null) {
            throw new BizException("INVALID_CREDENTIALS", "手机号或密码错误");
        }
        AccountAuthSnapshot snapshot = result.snapshot();
        return new AuthenticatedPrincipal(snapshot.userId(), snapshot.identityId(),
                snapshot.role(), snapshot.status());
    }
}
