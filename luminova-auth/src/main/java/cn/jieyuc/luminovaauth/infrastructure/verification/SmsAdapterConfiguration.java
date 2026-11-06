package cn.jieyuc.luminovaauth.infrastructure.verification;

import cn.jieyuc.luminovaauth.application.port.verification.SmsSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信 Adapter 装配配置。
 */
@Configuration
public class SmsAdapterConfiguration {

    /**
     * 未接入供应商时装配安全失败实现，防止验证码被写入日志或静默丢弃。
     */
    @Bean
    @ConditionalOnMissingBean(SmsSender.class)
    public SmsSender unconfiguredSmsSender() {
        return new UnconfiguredSmsSender();
    }
}
