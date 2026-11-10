package cn.jieyuc.luminova.base.response;

/** 与具体业务上下文无关的通用响应码。 */
public enum ApiResponseCode {
    SUCCESS("SUCCESS", "操作成功"),
    VALIDATION_FAILED("VALIDATION_FAILED", "请求参数校验失败"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误");

    private final String code;
    private final String defaultMessage;

    ApiResponseCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /** 返回对外稳定的机器可读码。 */
    public String code() {
        return code;
    }

    /** 返回不会泄漏内部实现的默认文案。 */
    public String defaultMessage() {
        return defaultMessage;
    }
}
