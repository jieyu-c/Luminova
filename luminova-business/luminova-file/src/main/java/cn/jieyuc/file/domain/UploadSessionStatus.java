package cn.jieyuc.file.domain;

/** 上传会话生命周期状态。 */
public enum UploadSessionStatus {
    ACTIVE,
    COMPLETING,
    COMPLETED,
    ABORTED,
    EXPIRED
}
