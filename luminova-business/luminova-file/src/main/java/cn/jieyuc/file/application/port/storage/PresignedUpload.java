package cn.jieyuc.file.application.port.storage;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

/** 对客户端安全公开的短时上传凭证。 */
public record PresignedUpload(URI url, Map<String, String> requiredHeaders, OffsetDateTime expiresAt) {
}
