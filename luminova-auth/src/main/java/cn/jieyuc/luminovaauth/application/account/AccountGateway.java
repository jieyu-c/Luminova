package cn.jieyuc.luminovaauth.application.account;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminova.api.account.PasswordAuthenticationResult;

/**
 * 认证上下文访问账号上下文的应用端口。
 *
 * <p>端口隐藏 Dubbo 和远程异常，只暴露认证用例需要的稳定能力。</p>
 */
public interface AccountGateway {

    /** 创建已完成手机号验证的用户。 */
    AccountAuthSnapshot registerPhoneUser(String phone, String password);

    /** 验证手机号密码，不返回密码哈希。 */
    PasswordAuthenticationResult authenticatePhonePassword(String phone, String password);

    /** 使用已验证手机号重置密码。 */
    AccountAuthSnapshot resetPhonePassword(String phone, String newPassword);
}
