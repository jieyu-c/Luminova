package cn.jieyuc.file.domain;

import java.time.OffsetDateTime;

/**
 * 云文件聚合。
 *
 * <p>维护文件从待上传、可用、软删除到物理清理的状态不变量，不负责对象存储远程调用。</p>
 */
public final class CloudFile {
    private final CloudFileId id;
    private final long uploaderId;
    private final BusinessOwnership ownership;
    private final UploadPurpose purpose;
    private final UploadDeclaration declaration;
    private final StorageLocation storageLocation;
    private CloudFileStatus status;
    private final OffsetDateTime createdAt;
    private OffsetDateTime availableAt;
    private OffsetDateTime deletedAt;
    private OffsetDateTime purgedAt;
    private int version;

    /** 从可信数据重建云文件聚合。 */
    public CloudFile(CloudFileId id, long uploaderId, BusinessOwnership ownership, UploadPurpose purpose,
                     UploadDeclaration declaration, StorageLocation storageLocation, CloudFileStatus status,
                     OffsetDateTime createdAt, OffsetDateTime availableAt, OffsetDateTime deletedAt,
                     OffsetDateTime purgedAt, int version) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.ownership = ownership;
        this.purpose = purpose;
        this.declaration = declaration;
        this.storageLocation = storageLocation;
        this.status = status;
        this.createdAt = createdAt;
        this.availableAt = availableAt;
        this.deletedAt = deletedAt;
        this.purgedAt = purgedAt;
        this.version = version;
    }

    /** 创建处于待上传状态的新文件。 */
    public static CloudFile create(CloudFileId id, long uploaderId, BusinessOwnership ownership,
                                   UploadPurpose purpose, UploadDeclaration declaration,
                                   StorageLocation location, OffsetDateTime now) {
        purpose.validate(declaration);
        return new CloudFile(id, uploaderId, ownership, purpose, declaration, location,
                CloudFileStatus.PENDING, now, null, null, null, 0);
    }

    /** 在完成存储校验后将文件标记为可用；重复确认保持幂等。 */
    public void markAvailable(OffsetDateTime now) {
        if (status == CloudFileStatus.AVAILABLE) {
            return;
        }
        if (status != CloudFileStatus.PENDING) {
            throw new IllegalStateException("只有待上传文件可以变为可用");
        }
        status = CloudFileStatus.AVAILABLE;
        availableAt = now;
    }

    /** 软删除可用文件；删除后不得签发新的访问地址。 */
    public void delete(OffsetDateTime now) {
        if (status == CloudFileStatus.DELETED) {
            return;
        }
        if (status != CloudFileStatus.AVAILABLE) {
            throw new IllegalStateException("只有可用文件可以删除");
        }
        status = CloudFileStatus.DELETED;
        deletedAt = now;
    }

    /** 在物理对象成功删除后将文件标记为不可恢复的已清理状态。 */
    public void purge(OffsetDateTime now) {
        if (status != CloudFileStatus.DELETED) {
            throw new IllegalStateException("只有已删除文件可以物理清理");
        }
        status = CloudFileStatus.PURGED;
        purgedAt = now;
    }

    public CloudFileId id() {
        return id;
    }

    public long uploaderId() {
        return uploaderId;
    }

    public BusinessOwnership ownership() {
        return ownership;
    }

    public UploadPurpose purpose() {
        return purpose;
    }

    public UploadDeclaration declaration() {
        return declaration;
    }

    public StorageLocation storageLocation() {
        return storageLocation;
    }

    public CloudFileStatus status() {
        return status;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime availableAt() {
        return availableAt;
    }

    public OffsetDateTime deletedAt() {
        return deletedAt;
    }

    public OffsetDateTime purgedAt() {
        return purgedAt;
    }

    public int version() {
        return version;
    }
}
