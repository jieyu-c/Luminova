package cn.jieyuc.file.domain;

/** 上传会话的稳定标识。 */
public record UploadSessionId(long value) {
    /** 创建标识并拒绝非正数。 */
    public UploadSessionId {
        if (value <= 0) {
            throw new IllegalArgumentException("上传会话标识必须为正数");
        }
    }
}
