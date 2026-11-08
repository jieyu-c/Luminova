package cn.jieyuc.file.domain;

import java.time.OffsetDateTime;

/** 业务对象对云文件的可追溯引用；相同引用键重复绑定或释放保持幂等。 */
public final class FileReference {
    private final long id;
    private final CloudFileId fileId;
    private final BusinessOwnership ownership;
    private final String referenceType;
    private final String referenceKey;
    private final OffsetDateTime createdAt;
    private OffsetDateTime releasedAt;

    /** 从可信数据重建引用。 */
    public FileReference(long id, CloudFileId fileId, BusinessOwnership ownership, String referenceType,
                         String referenceKey, OffsetDateTime createdAt, OffsetDateTime releasedAt) {
        if (id <= 0 || referenceType == null || referenceType.isBlank()
                || referenceKey == null || referenceKey.isBlank()) {
            throw new IllegalArgumentException("文件引用不完整");
        }
        this.id = id;
        this.fileId = fileId;
        this.ownership = ownership;
        this.referenceType = referenceType;
        this.referenceKey = referenceKey;
        this.createdAt = createdAt;
        this.releasedAt = releasedAt;
    }

    /** 创建活动引用。 */
    public static FileReference create(long id, CloudFileId fileId, BusinessOwnership ownership,
                                       String referenceType, String referenceKey, OffsetDateTime now) {
        return new FileReference(id, fileId, ownership, referenceType, referenceKey, now, null);
    }

    /** 释放引用；重复释放保持幂等并保留首次释放时间。 */
    public void release(OffsetDateTime now) {
        if (releasedAt == null) {
            releasedAt = now;
        }
    }

    public long id() {
        return id;
    }

    public CloudFileId fileId() {
        return fileId;
    }

    public BusinessOwnership ownership() {
        return ownership;
    }

    public String referenceType() {
        return referenceType;
    }

    public String referenceKey() {
        return referenceKey;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime releasedAt() {
        return releasedAt;
    }
}
