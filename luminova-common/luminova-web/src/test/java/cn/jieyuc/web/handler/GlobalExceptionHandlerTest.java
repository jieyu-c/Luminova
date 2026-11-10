package cn.jieyuc.web.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminova.base.response.ApiErrorType;
import cn.jieyuc.luminova.base.response.ApiResponse;
import cn.jieyuc.web.response.ApiResponseMetadataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** 全局异常到统一 API 响应的映射测试。 */
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler(new ApiResponseMetadataFactory());

    @Test
    @DisplayName("业务异常保留 HTTP 状态、业务码和请求标识")
    void shouldMapBusinessException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ApiResponseMetadataFactory.REQUEST_ID_HEADER, "req-2");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(
                new BizException(BizErrorCode.USER_NOT_FOUND), request, servletResponse);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals(BizErrorCode.USER_NOT_FOUND.code(), response.getBody().code());
        assertEquals(ApiErrorType.BUSINESS_ERROR, response.getBody().error().type());
        assertEquals("req-2", response.getBody().meta().requestId());
        assertEquals("req-2", servletResponse.getHeader(ApiResponseMetadataFactory.REQUEST_ID_HEADER));
    }

    @Test
    @DisplayName("未知异常固定返回 500 且不泄漏内部消息")
    void shouldHideUnexpectedException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpectedException(
                new RuntimeException("database password leaked"), request, servletResponse);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("系统内部错误", response.getBody().message());
        assertEquals(ApiErrorType.SYSTEM_ERROR, response.getBody().error().type());
    }
}
