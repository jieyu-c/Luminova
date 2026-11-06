package cn.jieyuc.luminovaauth.application.port.login;

import cn.jieyuc.luminovaauth.application.login.AuthenticatedPrincipal;
import cn.jieyuc.luminovaauth.application.login.LoginType;

/**
 * 可扩展登录认证策略。
 *
 * <p>策略只证明凭证对应哪个用户主体，不得签发 Token 或返回 HTTP 响应。</p>
 */
public interface LoginAuthenticator<C extends LoginCommand> {

    /** 返回该策略支持的登录类型。 */
    LoginType supports();

    /** 返回该策略接受的强类型命令。 */
    Class<C> commandType();

    /** 验证凭证并返回统一主体。 */
    AuthenticatedPrincipal authenticate(C command);
}
