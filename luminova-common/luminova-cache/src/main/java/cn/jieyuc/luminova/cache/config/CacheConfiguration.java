package cn.jieyuc.luminova.cache.config;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 缓存配置
 *
 * @author Hollis
 */
@AutoConfiguration
@EnableMethodCache(basePackages = "cn.jieyuc")
public class CacheConfiguration {
}
