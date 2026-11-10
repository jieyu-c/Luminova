package cn.jieyuc.web.handler;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminova.base.response.ApiError;
import cn.jieyuc.luminova.base.response.ApiErrorType;
import cn.jieyuc.luminova.base.response.ApiResponse;
import cn.jieyuc.luminova.base.response.ApiResponseCode;
import cn.jieyuc.luminova.base.response.ResponseMeta;
import cn.jieyuc.web.response.ApiResponseMetadataFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全部 HTTP 上下文共享的异常响应映射器。
 *
 * <p>业务异常保留其稳定错误码和 HTTP 状态；输入校验失败返回字段级问题；未预期异常仅在服务端
 * 记录异常类型，对客户端固定返回安全文案，避免异常消息携带敏感输入进入日志。</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String CONSTRAINT_VIOLATION_CODE = "CONSTRAINT_VIOLATION";
    private static final String INVALID_FIELD_CODE = "INVALID";
    private final ApiResponseMetadataFactory metadataFactory;

    /**
     * 创建全局异常映射器。
     *
     * @param metadataFactory 请求元数据工厂
     */
    public GlobalExceptionHandler(ApiResponseMetadataFactory metadataFactory) {
        this.metadataFactory = metadataFactory;
    }

    /** 将带稳定错误码的业务失败映射为对应 HTTP 状态。 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BizException exception,
            HttpServletRequest request,
            HttpServletResponse response) {
        HttpStatus status = HttpStatus.resolve(exception.getErrorCode().httpStatus());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        ApiError error = new ApiError(ApiErrorType.BUSINESS_ERROR, null, null, Collections.emptyMap());
        return failure(status, exception.getCode(), exception.getMessage(), error, request, response);
    }

    /** 将请求体字段校验失败映射为统一的 violations 列表。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request,
            HttpServletResponse response) {
        List<Map<String, Object>> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(this::fieldViolation)
                .toList();
        return validationFailure(violations, request, response);
    }

    /** 将方法参数约束失败映射为统一的 violations 列表。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request,
            HttpServletResponse response) {
        List<Map<String, Object>> violations = exception.getConstraintViolations().stream()
                .map(violation -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("field", violation.getPropertyPath().toString());
                    item.put("code", CONSTRAINT_VIOLATION_CODE);
                    item.put("message", violation.getMessage());
                    return item;
                })
                .toList();
        return validationFailure(violations, request, response);
    }

    /** 将应用边界主动拒绝的非法参数或状态映射为 400。 */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(
            RuntimeException exception,
            HttpServletRequest request,
            HttpServletResponse response) {
        ApiError error = new ApiError(ApiErrorType.BUSINESS_ERROR, null, null, Collections.emptyMap());
        return failure(HttpStatus.BAD_REQUEST, BizErrorCode.INVALID_ARGUMENT.code(),
                BizErrorCode.INVALID_ARGUMENT.defaultMessage(), error, request, response);
    }

    /** 兜底处理未预期异常，不向客户端暴露实现细节和堆栈。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 异常消息可能携带外部输入或敏感值，普通日志只记录异常类型并依赖 requestId/traceId 关联诊断。
        log.error("未处理的 HTTP 请求异常，exceptionType={}", exception.getClass().getName());
        ApiError error = new ApiError(ApiErrorType.SYSTEM_ERROR, null, null, Collections.emptyMap());
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, ApiResponseCode.INTERNAL_ERROR.code(),
                ApiResponseCode.INTERNAL_ERROR.defaultMessage(), error, request, response);
    }

    private Map<String, Object> fieldViolation(FieldError fieldError) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("field", fieldError.getField());
        item.put("code", fieldError.getCode() == null ? INVALID_FIELD_CODE : fieldError.getCode());
        item.put("message", fieldError.getDefaultMessage() == null ? "参数不合法" : fieldError.getDefaultMessage());
        return item;
    }

    private ResponseEntity<ApiResponse<Void>> validationFailure(
            List<Map<String, Object>> violations,
            HttpServletRequest request,
            HttpServletResponse response) {
        ApiError error = new ApiError(ApiErrorType.VALIDATION_ERROR, null, null,
                Map.of("violations", violations));
        return failure(HttpStatus.BAD_REQUEST, ApiResponseCode.VALIDATION_FAILED.code(),
                ApiResponseCode.VALIDATION_FAILED.defaultMessage(), error, request, response);
    }

    private ResponseEntity<ApiResponse<Void>> failure(
            HttpStatus status,
            String code,
            String message,
            ApiError error,
            HttpServletRequest request,
            HttpServletResponse response) {
        ResponseMeta meta = metadataFactory.create(request);
        response.setHeader(ApiResponseMetadataFactory.REQUEST_ID_HEADER, meta.requestId());
        return ResponseEntity.status(status).body(ApiResponse.failure(code, message, error, meta));
    }
}
