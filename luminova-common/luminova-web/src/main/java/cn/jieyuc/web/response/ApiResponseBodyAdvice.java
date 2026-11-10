package cn.jieyuc.web.response;

import cn.jieyuc.luminova.base.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 将普通 Controller JSON 返回值统一包装为 {@link ApiResponse}。
 *
 * <p>已经包装的响应不会重复包装；空响应、文本、二进制和流式协议保持原协议，以免破坏 204、
 * 文件下载、SSE 与 WebSocket。异常响应由统一异常处理器直接创建。</p>
 */
@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private static final List<MediaType> JSON_MEDIA_TYPES = List.of(
            MediaType.APPLICATION_JSON,
            MediaType.valueOf("application/*+json"));
    private final ApiResponseMetadataFactory metadataFactory;

    /**
     * 创建统一成功响应包装器。
     *
     * @param metadataFactory 请求元数据工厂
     */
    public ApiResponseBodyAdvice(ApiResponseMetadataFactory metadataFactory) {
        this.metadataFactory = metadataFactory;
    }

    /** 所有响应均进入运行时类型和媒体类型判断。 */
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /** 对 JSON 业务对象执行一次统一包装。 */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null || body instanceof ApiResponse<?> || body instanceof CharSequence
                || !isJson(selectedContentType) || !(request instanceof ServletServerHttpRequest servletRequest)) {
            return body;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        ApiResponse<?> apiResponse = ApiResponse.success(body, metadataFactory.create(httpRequest));
        response.getHeaders().set(ApiResponseMetadataFactory.REQUEST_ID_HEADER,
                apiResponse.meta().requestId());
        return apiResponse;
    }

    private boolean isJson(MediaType contentType) {
        return JSON_MEDIA_TYPES.stream().anyMatch(mediaType -> mediaType.isCompatibleWith(contentType));
    }
}
