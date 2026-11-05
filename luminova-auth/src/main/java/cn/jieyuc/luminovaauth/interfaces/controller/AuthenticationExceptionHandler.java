package cn.jieyuc.luminovaauth.interfaces.controller;

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
 * 认证接口异常到 HTTP 契约的映射器。
 *
 * <p>只返回稳定错误码和通用消息，不回显原始参数、密码、验证码或内部异常堆栈。</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "cn.jieyuc.luminovaauth")
public class AuthenticationExceptionHandler {

    /** 映射认证业务异常。 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiErrorResponse> handleBiz(BizException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getCode()) {
            case "INVALID_CREDENTIALS" -> HttpStatus.UNAUTHORIZED;
            case "RATE_LIMITED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "PHONE_ALREADY_REGISTERED", "PASSWORD_REUSED" -> HttpStatus.CONFLICT;
            case "SMS_PROVIDER_NOT_CONFIGURED" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                exception.getCode(), exception.getMessage(), requestId(request)));
    }

    /** 映射 DTO 校验失败。 */
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

    /**
     * 稳定的认证错误响应。
     */
    public record ApiErrorResponse(String code, String message, String requestId) {
    }
}
