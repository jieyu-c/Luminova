package cn.jieyuc.file.domain;

/** 云文件的稳定标识。 */
public record CloudFileId(long value) {
    /** 创建标识并拒绝非正数。 */
    public CloudFileId {
        if (value <= 0) {
            throw new IllegalArgumentException("云文件标识必须为正数");
        }
    }
}
