package cn.jieyuc.luminovaauth.application.login;

/**
 * 手机号密码登录命令。
 *
 * <p>password 为敏感凭证，该记录不得被写入日志或异常。</p>
 */
public record PhonePasswordLoginCommand(String phone, String password, String clientIp,
                                        String userAgent, String requestId) implements LoginCommand {
    @Override
    public LoginType loginType() {
        return LoginType.PHONE_PASSWORD;
    }
}
