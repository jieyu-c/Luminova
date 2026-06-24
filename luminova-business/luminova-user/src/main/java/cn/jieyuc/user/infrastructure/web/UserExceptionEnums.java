package cn.jieyuc.user.infrastructure.web;

import lombok.Getter;

@Getter
public enum UserExceptionEnums {
    USER_NOT_EXIST("用户不存在"),
    USER_EXIST("用户已存在"),
    USER_PASSWORD_ERROR("用户密码错误"),
    USER_PHONE_ERROR("用户手机号错误"),
    USER_PHONE_EXIST("用户手机号已存在"),
    USER_PHONE_NOT_EXIST("用户手机号不存在"),
    USER_PHONE_NOT_VERIFY("用户手机号未验证"),
    USER_PHONE_VERIFY_ERROR("用户手机号验证错误"),
    USERNAME_INVALID("用户名格式不正确"),
    USERNAME_TAKEN("用户名已被占用"),
    OLD_PASSWORD_WRONG("原密码错误"),
    AVATAR_INVALID("头像地址无效"),
    USER_ACCOUNT_UNAVAILABLE("用户账户不可用"),
    USER_REGISTER_RATE_LIMIT("注册过于频繁，请稍后再试"),
    VERIFY_CODE_RATE_LIMIT("验证码发送过于频繁，请稍后再试"),
    ;

    private String message;

    UserExceptionEnums(String message) {
        this.message = message;
    }
}
