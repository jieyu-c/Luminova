package cn.jieyuc.web.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import cn.jieyuc.luminova.base.response.ApiResponse;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Controller 返回值自动包装规则测试。 */
class ApiResponseBodyAdviceTest {
    private final ApiResponseBodyAdvice advice =
            new ApiResponseBodyAdvice(new ApiResponseMetadataFactory());

    @Test
    @DisplayName("JSON 业务对象包装为统一成功响应")
    void shouldWrapJsonBody() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ApiResponseMetadataFactory.REQUEST_ID_HEADER, "req-json");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletServerHttpResponse serverResponse = new ServletServerHttpResponse(response);

        Object result = advice.beforeBodyWrite(Map.of("id", 7L), null,
                MediaType.APPLICATION_JSON, null,
                new ServletServerHttpRequest(request), serverResponse);
        serverResponse.close();

        ApiResponse<?> apiResponse = assertInstanceOf(ApiResponse.class, result);
        assertEquals("SUCCESS", apiResponse.code());
        assertEquals("req-json", apiResponse.meta().requestId());
        assertEquals("req-json", response.getHeader(ApiResponseMetadataFactory.REQUEST_ID_HEADER));
    }

    @Test
    @DisplayName("文本响应保持原协议不包装")
    void shouldKeepTextBody() {
        String body = "plain text";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Object result = advice.beforeBodyWrite(body, null, MediaType.TEXT_PLAIN, null,
                new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));

        assertSame(body, result);
    }
}
