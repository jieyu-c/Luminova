package cn.jieyuc.file.infrastructure.persistence.conversion;

import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.UploadSession;
import cn.jieyuc.file.domain.UploadSessionId;
import cn.jieyuc.file.domain.UploadSessionStatus;
import cn.jieyuc.file.infrastructure.persistence.entity.UploadSessionEntity;
import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 上传会话聚合与持久化实体转换器。 */
@Mapper(config = DomainMappingConfig.class)
public interface UploadSessionPersistenceConverter {
    /** 转换为数据库实体，幂等键由仓储在首次插入时设置。 */
    @Mapping(target = "id", expression = "java(session.id().value())")
    @Mapping(target = "fileId", expression = "java(session.fileId().value())")
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "uploaderId", expression = "java(session.uploaderId())")
    @Mapping(target = "status", expression = "java(session.status().name())")
    @Mapping(target = "expiresAt", expression = "java(session.expiresAt())")
    @Mapping(target = "createdAt", expression = "java(session.createdAt())")
    @Mapping(target = "completedAt", expression = "java(session.completedAt())")
    @Mapping(target = "version", expression = "java(session.version())")
    UploadSessionEntity toEntity(UploadSession session);

    /** 从可信数据库行重建聚合。 */
    default UploadSession toDomain(UploadSessionEntity entity) {
        return new UploadSession(new UploadSessionId(entity.getId()), new CloudFileId(entity.getFileId()),
                entity.getUploaderId(), UploadSessionStatus.valueOf(entity.getStatus()), entity.getExpiresAt(),
                entity.getCreatedAt(), entity.getCompletedAt(), entity.getVersion());
    }
}
