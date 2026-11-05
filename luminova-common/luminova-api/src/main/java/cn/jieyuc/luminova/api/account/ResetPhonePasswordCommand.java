package cn.jieyuc.luminova.api.account;

import java.io.Serial;
import java.io.Serializable;

/**
 * 已完成手机号验证后的密码重置命令。
 */
public record ResetPhonePasswordCommand(String phone, String newPassword) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
