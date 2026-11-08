package cn.jieyuc.file.application;

import cn.jieyuc.file.domain.CloudFileStatus;
import cn.jieyuc.file.domain.UploadPurpose;
import java.time.OffsetDateTime;

/** 可安全返回给调用方的云文件元数据，不包含对象键和存储凭证。 */
public record CloudFileView(long fileId, UploadPurpose purpose, String originalFileName,
                            String mediaType, long size, String sha256, CloudFileStatus status,
                            OffsetDateTime createdAt, OffsetDateTime availableAt) {
}
