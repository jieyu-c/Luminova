package cn.jieyuc.web.response;

import cn.jieyuc.luminova.base.response.ResponseMeta;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * 从当前 HTTP 请求和日志追踪上下文创建统一响应元数据。
 *
 * <p>本类型不负责启动 Trace。上游没有提供请求 ID 时生成新的 ID；追踪系统尚未注入 traceId 时
 * 返回 {@code unknown}，避免伪造与实际链路不一致的追踪标识。</p>
 */
public class ApiResponseMetadataFactory {
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String UNKNOWN_TRACE_ID = "unknown";

    /** 根据当前 Servlet 请求创建响应元数据。 */
    public ResponseMeta create(HttpServletRequest request) {
        return new ResponseMeta(requestId(request), traceId(), Instant.now());
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }

    private String traceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isBlank()) {
            traceId = MDC.get("trace_id");
        }
        return traceId == null || traceId.isBlank() ? UNKNOWN_TRACE_ID : traceId;
    }
}
