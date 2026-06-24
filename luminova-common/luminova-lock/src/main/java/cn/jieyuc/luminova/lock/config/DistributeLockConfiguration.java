package cn.jieyuc.luminova.lock.config;

import cn.jieyuc.luminova.lock.logic.DistributeLockAspect;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Hollis
 */
@AutoConfiguration
public class DistributeLockConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DistributeLockAspect distributeLockAspect(RedissonClient redisson) {
        return new DistributeLockAspect(redisson);
    }
}
