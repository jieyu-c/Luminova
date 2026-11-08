package cn.jieyuc.file.domain;

import java.time.OffsetDateTime;

/** 上传会话聚合，维护有效期和完成状态转换。 */
public final class UploadSession {
    private final UploadSessionId id;
    private final CloudFileId fileId;
    private final long uploaderId;
    private UploadSessionStatus status;
    private final OffsetDateTime expiresAt;
    private final OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
    private int version;

    /** 从可信数据重建上传会话。 */
    public UploadSession(UploadSessionId id, CloudFileId fileId, long uploaderId, UploadSessionStatus status,
                         OffsetDateTime expiresAt, OffsetDateTime createdAt, OffsetDateTime completedAt, int version) {
        this.id = id;
        this.fileId = fileId;
        this.uploaderId = uploaderId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.version = version;
    }

    /** 创建新的活动上传会话。 */
    public static UploadSession create(UploadSessionId id, CloudFileId fileId, long uploaderId,
                                       OffsetDateTime now, OffsetDateTime expiresAt) {
        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("上传会话过期时间必须晚于创建时间");
        }
        return new UploadSession(id, fileId, uploaderId, UploadSessionStatus.ACTIVE,
                expiresAt, now, null, 0);
    }

    /** 校验当前用户拥有且会话仍处于可操作状态。 */
    public void requireActive(long operatorId, OffsetDateTime now) {
        if (uploaderId != operatorId) {
            throw new IllegalArgumentException("上传会话不属于当前用户");
        }
        if (!expiresAt.isAfter(now)) {
            status = UploadSessionStatus.EXPIRED;
            throw new IllegalStateException("上传会话已过期");
        }
        if (status != UploadSessionStatus.ACTIVE) {
            throw new IllegalStateException("上传会话当前不可操作");
        }
    }

    /** 进入完成中状态，避免并发完成请求重复推进远程副作用。 */
    public void startCompleting(long operatorId, OffsetDateTime now) {
        if (uploaderId != operatorId) {
            throw new IllegalArgumentException("上传会话不属于当前用户");
        }
        if (status == UploadSessionStatus.COMPLETING || status == UploadSessionStatus.COMPLETED) {
            return;
        }
        requireActive(operatorId, now);
        status = UploadSessionStatus.COMPLETING;
    }

    /** 在对象校验成功后完成会话。 */
    public void complete(OffsetDateTime now) {
        if (status == UploadSessionStatus.COMPLETED) {
            return;
        }
        if (status != UploadSessionStatus.COMPLETING) {
            throw new IllegalStateException("会话未进入完成中状态");
        }
        status = UploadSessionStatus.COMPLETED;
        completedAt = now;
    }

    /** 中止仍处于活动状态的会话。 */
    public void abort(long operatorId, OffsetDateTime now) {
        requireActive(operatorId, now);
        status = UploadSessionStatus.ABORTED;
    }

    public UploadSessionId id() {
        return id;
    }

    public CloudFileId fileId() {
        return fileId;
    }

    public long uploaderId() {
        return uploaderId;
    }

    public UploadSessionStatus status() {
        return status;
    }

    public OffsetDateTime expiresAt() {
        return expiresAt;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime completedAt() {
        return completedAt;
    }

    public int version() {
        return version;
    }
}
