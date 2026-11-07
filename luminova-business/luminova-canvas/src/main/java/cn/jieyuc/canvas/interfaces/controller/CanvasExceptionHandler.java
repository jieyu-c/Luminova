package cn.jieyuc.canvas.interfaces.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 工作台业务异常到稳定 HTTP 错误契约的映射器。 */
@RestControllerAdvice(basePackages = "cn.jieyuc.canvas")
public class CanvasExceptionHandler {
    /** 参数或业务不变量失败统一映射为 400，且不回显内部对象。 */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class,
            MethodArgumentNotValidException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception,
                                                             HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "INVALID_ARGUMENT", exception.getMessage(),
                requestId == null || requestId.isBlank() ? "unknown" : requestId));
    }

    /** 稳定错误响应。 */
    public record ApiErrorResponse(String code, String message, String requestId) {
    }
}
