package cn.jieyuc.luminova.base.exceptions;

/**
 * 对外稳定的业务错误码。
 *
 * <p>调用方只引用枚举常量。HTTP 响应、审计原因和日志里的机器可读码使用 {@link #code()}，
 * 不使用枚举常量名。限流的几项对外同为 {@code RATE_LIMITED}，常量分开只是为了固定不同文案。
 * {@link #httpStatus()} 是接口契约，由异常适配器读取，不在领域里判断协议。</p>
 *
 * <p>本类型不携带密码、验证码、Token 或完整手机号，也不决定事务和重试。</p>
 */
public enum BizErrorCode {
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "手机号或密码错误", 401),
    RATE_LIMITED("RATE_LIMITED", "请求过于频繁，请稍后重试", 429),
    /** 对外同为 RATE_LIMITED，文案对应登录连续失败锁定。 */
    LOGIN_ATTEMPT_RATE_LIMITED("RATE_LIMITED", "登录尝试过多，请稍后重试", 429),
    /** 对外同为 RATE_LIMITED，文案对应验证码发送频率。 */
    VERIFICATION_CODE_RATE_LIMITED("RATE_LIMITED", "验证码请求过于频繁，请稍后重试", 429),
    PHONE_ALREADY_REGISTERED("PHONE_ALREADY_REGISTERED", "手机号已注册", 409),
    PASSWORD_REUSED("PASSWORD_REUSED", "新密码不能与当前密码相同", 409),
    SMS_PROVIDER_NOT_CONFIGURED("SMS_PROVIDER_NOT_CONFIGURED", "短信服务暂不可用", 503),
    INVALID_ARGUMENT("INVALID_ARGUMENT", "请求参数不合法", 400),
    FORBIDDEN("FORBIDDEN", "无权访问", 403),
    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在", 404),
    CONCURRENT_MODIFICATION("CONCURRENT_MODIFICATION", "用户数据已被其他请求修改，请重试", 409),
    VERIFICATION_CODE_INVALID("VERIFICATION_CODE_INVALID", "验证码无效或已过期", 400),
    PASSWORD_LOGIN_UNAVAILABLE("PASSWORD_LOGIN_UNAVAILABLE", "当前账号未启用密码登录", 400),
    INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "当前密码错误", 400);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    BizErrorCode(String code, String defaultMessage, int httpStatus) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("业务错误码不能为空");
        }
        if (defaultMessage == null || defaultMessage.isBlank()) {
            throw new IllegalArgumentException("业务错误文案不能为空");
        }
        if (httpStatus < 100 || httpStatus > 599) {
            throw new IllegalArgumentException("HTTP 状态码不合法");
        }
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    /** 写入响应和审计的稳定错误码。 */
    public String code() {
        return code;
    }

    /** 不泄漏内部原因的默认文案。 */
    public String defaultMessage() {
        return defaultMessage;
    }

    /** 该错误对应的 HTTP 状态码。 */
    public int httpStatus() {
        return httpStatus;
    }
}
