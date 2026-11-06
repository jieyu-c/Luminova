package cn.jieyuc.luminovaauth.interfaces.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.jieyuc.luminova.base.exceptions.BizErrorCode;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.interfaces.controller.AuthenticationExceptionHandler.ApiErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 认证错误码到 HTTP 契约的映射。
 */
class AuthenticationExceptionHandlerTest {
    private final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();

    @Test
    @DisplayName("限流细分常量对外仍是 RATE_LIMITED，并映射为 429")
    void shouldMapRateLimitToTooManyRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "req-1");

        ResponseEntity<ApiErrorResponse> response = handler.handleBiz(
                new BizException(BizErrorCode.LOGIN_ATTEMPT_RATE_LIMITED), request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(BizErrorCode.RATE_LIMITED.code(), response.getBody().code());
        assertEquals(BizErrorCode.LOGIN_ATTEMPT_RATE_LIMITED.defaultMessage(), response.getBody().message());
        assertEquals("req-1", response.getBody().requestId());
    }

    @Test
    @DisplayName("参数校验失败使用 INVALID_ARGUMENT，不回显原始异常")
    void shouldHideArgumentDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(
                new IllegalArgumentException("手机号 13800138000 不合法"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(BizErrorCode.INVALID_ARGUMENT.code(), response.getBody().code());
        assertEquals(BizErrorCode.INVALID_ARGUMENT.defaultMessage(), response.getBody().message());
    }
}
