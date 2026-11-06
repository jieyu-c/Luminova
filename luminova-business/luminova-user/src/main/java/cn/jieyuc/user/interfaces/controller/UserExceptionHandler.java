package cn.jieyuc.user.interfaces.controller;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 用户接口异常到 HTTP 契约的映射器。
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "cn.jieyuc.user")
public class UserExceptionHandler {

    /** 映射用户上下文业务异常。状态来自错误码，避免在协议层再维护一套字符串。 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiErrorResponse> handleBiz(BizException exception, HttpServletRequest request) {
        return ResponseEntity.status(statusOf(exception)).body(new ApiErrorResponse(
                exception.getCode(), exception.getMessage(), requestId(request)));
    }

    /** 参数不合法时只返回稳定错误码，不回显原始异常内容。 */
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception,
                                                             HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                BizErrorCode.INVALID_ARGUMENT.code(),
                BizErrorCode.INVALID_ARGUMENT.defaultMessage(),
                requestId(request)));
    }

    private HttpStatus statusOf(BizException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getErrorCode().httpStatus());
        // 无法识别的状态按 400 返回，避免把内部故障暴露成 500。
        if (status == null) {
            return HttpStatus.BAD_REQUEST;
        }
        return status;
    }

    private String requestId(HttpServletRequest request) {
        String value = request.getHeader("X-Request-Id");
        return value == null || value.isBlank() ? "unknown" : value;
    }

    /** 稳定用户错误响应。 */
    public record ApiErrorResponse(String code, String message, String requestId) {
    }
}
