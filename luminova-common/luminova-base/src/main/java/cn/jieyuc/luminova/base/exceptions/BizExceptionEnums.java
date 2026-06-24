package cn.jieyuc.luminova.base.exceptions;

public enum BizExceptionEnums {
    GENERIC_ERROR("通用失败");

    private String message;

    BizExceptionEnums(String message) {
        this.message = message;
    }
}
