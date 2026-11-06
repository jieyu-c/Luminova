package cn.jieyuc.luminovaauth.application.port.login;

import cn.jieyuc.luminovaauth.application.login.LoginType;

/**
 * 登录策略的统一命令接口。
 *
 * <p>不同登录方式使用独立强类型命令，避免把任意 Map 传入核心认证流程。</p>
 */
public interface LoginCommand {

    /** 返回命令对应的登录策略类型。 */
    LoginType loginType();
}
