package cn.jieyuc.file.application.port.storage;

import java.net.URI;
import java.time.OffsetDateTime;

/** 对客户端安全公开的短时文件访问凭证。 */
public record PresignedAccess(URI url, OffsetDateTime expiresAt) {
}
