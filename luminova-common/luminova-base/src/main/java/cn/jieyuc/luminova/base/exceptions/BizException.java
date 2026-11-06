package cn.jieyuc.luminova.base.exceptions;

import java.util.Objects;

/**
 * 带稳定错误码的业务异常。
 *
 * <p>只携带 {@link BizErrorCode} 和给调用方看的默认文案。不得把密码、验证码、Token
 * 或完整手机号放进异常。对外错误码由枚举决定，调用方不能再传入裸字符串。</p>
 */
public class BizException extends RuntimeException {
    private final BizErrorCode errorCode;

    /**
     * 用错误码的默认文案构造业务异常。
     *
     * @param errorCode 稳定业务错误码，不能为空
     */
    public BizException(BizErrorCode errorCode) {
        super(Objects.requireNonNull(errorCode, "业务错误码不能为空").defaultMessage());
        this.errorCode = errorCode;
    }

    /** 触发本次失败的业务错误码。 */
    public BizErrorCode getErrorCode() {
        return errorCode;
    }

    /** 对外响应和审计使用的稳定错误码。 */
    public String getCode() {
        return errorCode.code();
    }
}
