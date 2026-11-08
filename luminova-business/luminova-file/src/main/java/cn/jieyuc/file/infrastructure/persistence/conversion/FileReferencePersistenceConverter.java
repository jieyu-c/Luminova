package cn.jieyuc.file.infrastructure.persistence.conversion;

import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.FileReference;
import cn.jieyuc.file.infrastructure.persistence.entity.FileReferenceEntity;
import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 文件引用领域对象与持久化实体转换器。 */
@Mapper(config = DomainMappingConfig.class)
public interface FileReferencePersistenceConverter {
    /** 转换为数据库实体。 */
    @Mapping(target = "id", expression = "java(reference.id())")
    @Mapping(target = "fileId", expression = "java(reference.fileId().value())")
    @Mapping(target = "businessContext", expression = "java(reference.ownership().contextCode())")
    @Mapping(target = "businessObjectType", expression = "java(reference.ownership().objectType())")
    @Mapping(target = "businessObjectId", expression = "java(reference.ownership().objectId())")
    @Mapping(target = "referenceType", expression = "java(reference.referenceType())")
    @Mapping(target = "referenceKey", expression = "java(reference.referenceKey())")
    @Mapping(target = "createdAt", expression = "java(reference.createdAt())")
    @Mapping(target = "releasedAt", expression = "java(reference.releasedAt())")
    FileReferenceEntity toEntity(FileReference reference);

    /** 从可信数据库行重建引用。 */
    default FileReference toDomain(FileReferenceEntity entity) {
        return new FileReference(entity.getId(), new CloudFileId(entity.getFileId()),
                new BusinessOwnership(entity.getBusinessContext(), entity.getBusinessObjectType(),
                        entity.getBusinessObjectId()), entity.getReferenceType(), entity.getReferenceKey(),
                entity.getCreatedAt(), entity.getReleasedAt());
    }
}
