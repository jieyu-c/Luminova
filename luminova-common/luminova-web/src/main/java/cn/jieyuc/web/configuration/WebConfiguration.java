package cn.jieyuc.web.configuration;
import cn.jieyuc.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Hollis
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class WebConfiguration implements WebMvcConfigurer {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalWebExceptionHandler() {
        return new GlobalExceptionHandler();
    }

}
