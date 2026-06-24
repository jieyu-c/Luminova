package cn.jieyuc.web.handler;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminova.base.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static cn.jieyuc.web.constant.WebErrCode.BAD_REQUEST;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BaseResponse handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数错误：{}", e.getMessage());
        return BaseResponse.error(BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BaseResponse handleBizException(BizException e) {
        log.error("业务异常：{}", e.getMessage());
        return BaseResponse.error(e.getCode(), e.getMessage());
    }
}
