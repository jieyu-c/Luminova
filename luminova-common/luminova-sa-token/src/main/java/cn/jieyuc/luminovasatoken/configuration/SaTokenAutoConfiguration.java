package cn.jieyuc.luminovasatoken.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "luminova.sa-token", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LuminovaSaTokenProperties.class)
public class SaTokenAutoConfiguration {

    @Bean
    public WebMvcConfigurer saTokenWebMvcConfigurer(LuminovaSaTokenProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new SaTokenLoginInterceptor())
                        .addPathPatterns(properties.getIncludePaths())
                        .excludePathPatterns(properties.getExcludePaths());
            }
        };
    }

    private static class SaTokenLoginInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws IOException {
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }
            if (StpUtil.isLogin()) {
                return true;
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"未登录\"}");
            return false;
        }
    }
}
