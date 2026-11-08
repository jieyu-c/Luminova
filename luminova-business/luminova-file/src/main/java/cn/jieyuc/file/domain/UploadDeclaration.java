package cn.jieyuc.file.domain;

import java.util.Locale;

/** 客户端在上传前声明且创建后不可修改的文件属性。 */
public record UploadDeclaration(String originalFileName, String mediaType, long size, String sha256) {
    /** 创建声明并规范化 SHA-256。 */
    public UploadDeclaration {
        if (originalFileName == null || originalFileName.isBlank() || originalFileName.length() > 255) {
            throw new IllegalArgumentException("文件名不能为空且不能超过255个字符");
        }
        if (mediaType == null || mediaType.isBlank() || size <= 0) {
            throw new IllegalArgumentException("媒体类型和文件大小不合法");
        }
        if (sha256 == null || !sha256.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("SHA-256必须为64位十六进制字符串");
        }
        sha256 = sha256.toLowerCase(Locale.ROOT);
    }
}
