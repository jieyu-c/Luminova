package cn.jieyuc.file.application;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

/** 创建上传会话后的客户端视图。 */
public record InitiatedFileUpload(long sessionId, long fileId, URI uploadUrl,
                                  Map<String, String> requiredHeaders, OffsetDateTime expiresAt) {
}
