package cn.jieyuc.file.infrastructure.storage;

import cn.jieyuc.file.application.port.storage.ObjectStoragePort;
import cn.jieyuc.file.application.port.storage.StorageProviderRegistry;
import java.time.Clock;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/** 创建 RustFS 客户端、预签名器和对象存储注册表。 */
@Configuration
@EnableConfigurationProperties(RustFsStorageProperties.class)
public class FileStorageConfiguration {
    /** 提供可由测试替换的 UTC 时钟。 */
    @Bean
    @ConditionalOnMissingBean
    public Clock fileClock() {
        return Clock.systemUTC();
    }

    /** 创建连接 RustFS S3 API 的同步客户端。 */
    @Bean(destroyMethod = "close")
    public S3Client rustFsS3Client(RustFsStorageProperties properties) {
        validate(properties);
        return S3Client.builder().endpointOverride(properties.endpoint()).region(Region.of(properties.region()))
                .credentialsProvider(credentials(properties))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(properties.pathStyle()).build())
                .httpClientBuilder(UrlConnectionHttpClient.builder().connectionTimeout(properties.connectTimeout()))
                .overrideConfiguration(builder -> builder.apiCallTimeout(properties.requestTimeout())).build();
    }

    /** 创建与客户端使用相同 Endpoint 和签名配置的预签名器。 */
    @Bean(destroyMethod = "close")
    public S3Presigner rustFsS3Presigner(RustFsStorageProperties properties) {
        validate(properties);
        return S3Presigner.builder().endpointOverride(properties.endpoint()).region(Region.of(properties.region()))
                .credentialsProvider(credentials(properties))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(properties.pathStyle()).build())
                .build();
    }

    /** 创建 RustFS Adapter。 */
    @Bean
    public RustFsObjectStorageAdapter rustFsObjectStorageAdapter(S3Client client, S3Presigner presigner, Clock clock) {
        return new RustFsObjectStorageAdapter(client, presigner, clock);
    }

    /** 收集已启用对象存储 Adapter 并校验唯一性。 */
    @Bean
    public StorageProviderRegistry storageProviderRegistry(List<ObjectStoragePort> ports) {
        return new StorageProviderRegistry(ports);
    }

    private StaticCredentialsProvider credentials(RustFsStorageProperties properties) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
    }

    private void validate(RustFsStorageProperties properties) {
        if (properties.endpoint() == null || properties.accessKey() == null || properties.accessKey().isBlank()
                || properties.secretKey() == null || properties.secretKey().isBlank()) {
            throw new IllegalStateException("RustFS Endpoint 和凭证必须由外部配置提供");
        }
    }
}
