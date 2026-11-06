package cn.jieyuc.user.interfaces.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.interfaces.controller.UserExceptionHandler.ApiErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 用户错误码到 HTTP 契约的映射。
 */
class UserExceptionHandlerTest {
    private final UserExceptionHandler handler = new UserExceptionHandler();

    @Test
    @DisplayName("用户不存在映射为 404，并发修改映射为 409")
    void shouldMapUserErrorsToHttpStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "req-2");

        ResponseEntity<ApiErrorResponse> missing = handler.handleBiz(
                new BizException(BizErrorCode.USER_NOT_FOUND), request);
        ResponseEntity<ApiErrorResponse> conflict = handler.handleBiz(
                new BizException(BizErrorCode.CONCURRENT_MODIFICATION), request);

        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals(BizErrorCode.USER_NOT_FOUND.code(), missing.getBody().code());
        assertEquals("req-2", missing.getBody().requestId());
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals(BizErrorCode.CONCURRENT_MODIFICATION.code(), conflict.getBody().code());
    }
}
