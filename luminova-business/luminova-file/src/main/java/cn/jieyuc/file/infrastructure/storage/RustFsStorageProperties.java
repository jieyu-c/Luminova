package cn.jieyuc.file.infrastructure.storage;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** RustFS S3 兼容连接配置；凭证只允许由外部配置注入。 */
@ConfigurationProperties("luminova.file.storage")
public record RustFsStorageProperties(URI endpoint, String region, String accessKey, String secretKey,
                                      String bucket, boolean pathStyle, Duration connectTimeout,
                                      Duration requestTimeout) {
    /** 补齐非敏感默认值并校验凭证。 */
    public RustFsStorageProperties {
        region = region == null || region.isBlank() ? "us-east-1" : region;
        bucket = bucket == null || bucket.isBlank() ? "luminova-private" : bucket;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(3) : connectTimeout;
        requestTimeout = requestTimeout == null ? Duration.ofSeconds(10) : requestTimeout;
    }
}
