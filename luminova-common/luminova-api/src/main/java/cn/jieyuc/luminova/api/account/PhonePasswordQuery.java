package cn.jieyuc.luminova.api.account;

import java.io.Serial;
import java.io.Serializable;

/**
 * 手机号密码认证查询。
 *
 * <p>该对象不得输出到日志，避免原始密码进入日志和链路追踪系统。</p>
 */
public record PhonePasswordQuery(String phone, String password) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
