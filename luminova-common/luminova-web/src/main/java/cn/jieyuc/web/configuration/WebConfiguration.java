package cn.jieyuc.web.configuration;

import cn.jieyuc.web.handler.GlobalExceptionHandler;
import cn.jieyuc.web.response.ApiResponseBodyAdvice;
import cn.jieyuc.web.response.ApiResponseMetadataFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Luminova Servlet Web 公共自动配置。
 *
 * <p>统一注册响应包装、请求元数据和异常映射，不负责具体业务 Controller 或鉴权规则。</p>
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class WebConfiguration implements WebMvcConfigurer {

    /** 注册请求响应元数据工厂。 */
    @Bean
    @ConditionalOnMissingBean
    public ApiResponseMetadataFactory apiResponseMetadataFactory() {
        return new ApiResponseMetadataFactory();
    }

    /** 注册 JSON 成功响应自动包装器。 */
    @Bean
    @ConditionalOnMissingBean
    public ApiResponseBodyAdvice apiResponseBodyAdvice(ApiResponseMetadataFactory metadataFactory) {
        return new ApiResponseBodyAdvice(metadataFactory);
    }

    /** 注册全局失败响应映射器。 */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalWebExceptionHandler(ApiResponseMetadataFactory metadataFactory) {
        return new GlobalExceptionHandler(metadataFactory);
    }

}
