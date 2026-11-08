package cn.jieyuc.file.application.port.storage;

/** 对象存储返回的可验证元数据。 */
public record StoredObjectMetadata(long size, String contentType, String declaredSha256) {
}
