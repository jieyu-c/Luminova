package cn.jieyuc.file.interfaces.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 将文件业务失败映射为稳定且不泄漏对象位置的 HTTP 错误。 */
@RestControllerAdvice(basePackages = "cn.jieyuc.file")
public class FileExceptionHandler {
    /** 参数、状态和权限失败暂统一映射为客户端错误。 */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class,
            MethodArgumentNotValidException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return ResponseEntity.badRequest().body(new ApiErrorResponse("FILE_REQUEST_REJECTED", exception.getMessage(),
                requestId == null || requestId.isBlank() ? "unknown" : requestId));
    }

    /** 稳定错误响应。 */
    public record ApiErrorResponse(String code, String message, String requestId) {
    }
}
