package cn.jieyuc.user.interfaces.controller;

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

    /** 映射用户上下文业务异常。 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiErrorResponse> handleBiz(BizException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getCode()) {
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "PHONE_ALREADY_REGISTERED", "PASSWORD_REUSED", "CONCURRENT_MODIFICATION" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                exception.getCode(), exception.getMessage(), requestId(request)));
    }

    /** 映射参数校验失败。 */
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception,
                                                             HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "INVALID_ARGUMENT", "请求参数不合法", requestId(request)));
    }

    private String requestId(HttpServletRequest request) {
        String value = request.getHeader("X-Request-Id");
        return value == null || value.isBlank() ? "unknown" : value;
    }

    /** 稳定用户错误响应。 */
    public record ApiErrorResponse(String code, String message, String requestId) {
    }
}
