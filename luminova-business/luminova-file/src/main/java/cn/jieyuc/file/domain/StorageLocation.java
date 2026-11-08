package cn.jieyuc.file.domain;

/** 仅供文件上下文内部使用的对象存储位置。 */
public record StorageLocation(StorageProviderCode providerCode, String bucket, String objectKey) {
    /** 创建位置并拒绝空 Bucket 或对象键。 */
    public StorageLocation {
        if (providerCode == null || bucket == null || bucket.isBlank() || objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("存储位置不完整");
        }
    }
}
