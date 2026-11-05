package cn.jieyuc.luminova.api.account;

import java.io.Serial;
import java.io.Serializable;

/**
 * 手机号用户注册命令。
 *
 * <p>password 为敏感凭证，禁止记录、序列化到响应或出现在异常信息中。</p>
 */
public record RegisterPhoneUserCommand(String phone, String password) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
