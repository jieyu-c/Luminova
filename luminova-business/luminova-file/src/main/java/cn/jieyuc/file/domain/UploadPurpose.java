package cn.jieyuc.file.domain;

import java.util.Set;

/** MVP 支持的上传用途及其文件策略。 */
public enum UploadPurpose {
    CANVAS_ASSET(200L * 1024 * 1024, Set.of("image/", "video/", "audio/", "application/octet-stream")),
    WORKSPACE_COVER(20L * 1024 * 1024, Set.of("image/"));

    private final long maximumSize;
    private final Set<String> mediaTypePrefixes;

    UploadPurpose(long maximumSize, Set<String> mediaTypePrefixes) {
        this.maximumSize = maximumSize;
        this.mediaTypePrefixes = mediaTypePrefixes;
    }

    /** 校验声明是否满足当前用途的大小和媒体类型策略。 */
    public void validate(UploadDeclaration declaration) {
        if (declaration.size() > maximumSize) {
            throw new IllegalArgumentException("文件大小超过用途限制");
        }
        if (mediaTypePrefixes.stream().noneMatch(declaration.mediaType()::startsWith)) {
            throw new IllegalArgumentException("媒体类型不符合上传用途");
        }
    }
}
