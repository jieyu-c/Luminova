package cn.jieyuc.luminova.base.exceptions;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    // 异常码
    private String code;
    // 异常信息
    private String message;

    public BizException(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
