package cn.jieyuc.luminovaauth.application.login;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 登录策略注册表。
 *
 * <p>启动时检查重复策略，运行时集中完成受控类型转换。统一登录服务不需要随着策略增加
 * 而添加 switch 或 if/else。</p>
 */
@Component
public class LoginAuthenticatorRegistry {
    private final Map<LoginType, LoginAuthenticator<?>> authenticators;

    public LoginAuthenticatorRegistry(List<LoginAuthenticator<?>> candidates) {
        EnumMap<LoginType, LoginAuthenticator<?>> registry = new EnumMap<>(LoginType.class);
        for (LoginAuthenticator<?> candidate : candidates) {
            if (registry.putIfAbsent(candidate.supports(), candidate) != null) {
                throw new IllegalStateException("存在重复登录策略: " + candidate.supports());
            }
        }
        this.authenticators = Map.copyOf(registry);
    }

    /**
     * 使用命令类型匹配的认证策略执行认证。
     *
     * @param command 强类型登录命令
     * @return 统一认证主体
     */
    public AuthenticatedPrincipal authenticate(LoginCommand command) {
        LoginAuthenticator<?> authenticator = authenticators.get(command.loginType());
        if (authenticator == null || !authenticator.commandType().isInstance(command)) {
            throw new IllegalArgumentException("不支持的登录方式");
        }
        return invoke(authenticator, command);
    }

    private <C extends LoginCommand> AuthenticatedPrincipal invoke(LoginAuthenticator<C> authenticator,
                                                                    LoginCommand command) {
        return authenticator.authenticate(authenticator.commandType().cast(command));
    }
}
