package cn.jieyuc.file.infrastructure.storage;

import cn.jieyuc.file.application.port.storage.ObjectStoragePort;
import cn.jieyuc.file.application.port.storage.PresignedAccess;
import cn.jieyuc.file.application.port.storage.PresignedUpload;
import cn.jieyuc.file.application.port.storage.StoredObjectMetadata;
import cn.jieyuc.file.domain.StorageLocation;
import cn.jieyuc.file.domain.StorageProviderCode;
import cn.jieyuc.file.domain.UploadDeclaration;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

/**
 * 通过 AWS SDK v2 对接 RustFS S3 API 的对象存储 Adapter。
 *
 * <p>所有签名地址均为短时凭证，异常只转换为不包含密钥和签名 URL 的统一运行时异常。</p>
 */
public class RustFsObjectStorageAdapter implements ObjectStoragePort {
    private static final String SHA256_METADATA = "luminova-sha256";
    private final S3Client client;
    private final S3Presigner presigner;
    private final Clock clock;

    public RustFsObjectStorageAdapter(S3Client client, S3Presigner presigner, Clock clock) {
        this.client = client;
        this.presigner = presigner;
        this.clock = clock;
    }

    /** 返回 RustFS 提供方代号。 */
    @Override
    public StorageProviderCode providerCode() {
        return StorageProviderCode.RUSTFS;
    }

    /** 签发绑定长度、类型和声明哈希元数据的 PUT 地址。 */
    @Override
    public PresignedUpload issueSingleUpload(StorageLocation location, UploadDeclaration declaration,
                                             Duration validity) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(location.bucket()).key(location.objectKey())
                    .contentLength(declaration.size()).contentType(declaration.mediaType())
                    .metadata(Map.of(SHA256_METADATA, declaration.sha256())).build();
            PresignedPutObjectRequest signed = presigner.presignPutObject(PutObjectPresignRequest.builder()
                    .signatureDuration(validity).putObjectRequest(request).build());
            return new PresignedUpload(signed.url().toURI(), flatten(signed.signedHeaders()), expiresAt(validity));
        } catch (Exception exception) {
            throw storageFailure("签发上传地址失败", exception);
        }
    }

    /** 使用 HeadObject 获取完成校验所需元数据。 */
    @Override
    public StoredObjectMetadata inspect(StorageLocation location) {
        try {
            HeadObjectResponse response = client.headObject(HeadObjectRequest.builder()
                    .bucket(location.bucket()).key(location.objectKey()).build());
            return new StoredObjectMetadata(response.contentLength(), response.contentType(),
                    response.metadata().get(SHA256_METADATA));
        } catch (SdkException exception) {
            throw storageFailure("读取对象元数据失败", exception);
        }
    }

    /** 签发私有对象的短时下载地址，并安全设置下载文件名。 */
    @Override
    public PresignedAccess issueAccess(StorageLocation location, String downloadName, Duration validity) {
        try {
            String disposition = "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(downloadName,
                    java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            GetObjectRequest request = GetObjectRequest.builder().bucket(location.bucket()).key(location.objectKey())
                    .responseContentDisposition(disposition).build();
            PresignedGetObjectRequest signed = presigner.presignGetObject(GetObjectPresignRequest.builder()
                    .signatureDuration(validity).getObjectRequest(request).build());
            return new PresignedAccess(signed.url().toURI(), expiresAt(validity));
        } catch (Exception exception) {
            throw storageFailure("签发访问地址失败", exception);
        }
    }

    /** 删除对象；S3 删除不存在对象同样成功，因此调用天然幂等。 */
    @Override
    public void delete(StorageLocation location) {
        try {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(location.bucket()).key(location.objectKey()).build());
        } catch (SdkException exception) {
            throw storageFailure("删除对象失败", exception);
        }
    }

    private OffsetDateTime expiresAt(Duration validity) {
        return OffsetDateTime.ofInstant(clock.instant().plus(validity), ZoneOffset.UTC);
    }

    private Map<String, String> flatten(Map<String, List<String>> headers) {
        Map<String, String> result = new LinkedHashMap<>();
        headers.forEach((name, values) -> result.put(name, String.join(",", values)));
        return Map.copyOf(result);
    }

    private IllegalStateException storageFailure(String message, Exception cause) {
        return new IllegalStateException(message, cause);
    }
}
