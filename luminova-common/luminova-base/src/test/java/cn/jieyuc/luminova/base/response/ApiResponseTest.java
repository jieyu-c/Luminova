package cn.jieyuc.luminova.base.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 统一 API 响应不变量测试。 */
class ApiResponseTest {

    @Test
    @DisplayName("成功响应包含数据且不包含错误")
    void shouldCreateSuccessResponse() {
        ResponseMeta meta = new ResponseMeta("req-1", "trace-1", Instant.EPOCH);

        ApiResponse<Map<String, Long>> response = ApiResponse.success(Map.of("id", 1L), meta);

        assertTrue(response.success());
        assertEquals(ApiResponseCode.SUCCESS.code(), response.code());
        assertEquals(1L, response.data().get("id"));
        assertNull(response.error());
        assertEquals(meta, response.meta());
    }

    @Test
    @DisplayName("失败响应包含错误且不包含成功数据")
    void shouldCreateFailureResponse() {
        ResponseMeta meta = new ResponseMeta("req-2", "trace-2", Instant.EPOCH);
        ApiError error = new ApiError(ApiErrorType.BUSINESS_ERROR, null, null, Map.of());

        ApiResponse<Void> response = ApiResponse.failure("USER_NOT_FOUND", "用户不存在", error, meta);

        assertFalse(response.success());
        assertEquals("USER_NOT_FOUND", response.code());
        assertNull(response.data());
        assertEquals(error, response.error());
    }
}
