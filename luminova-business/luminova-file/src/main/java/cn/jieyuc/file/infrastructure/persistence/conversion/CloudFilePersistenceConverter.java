package cn.jieyuc.file.infrastructure.persistence.conversion;

import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.CloudFileStatus;
import cn.jieyuc.file.domain.StorageLocation;
import cn.jieyuc.file.domain.StorageProviderCode;
import cn.jieyuc.file.domain.UploadDeclaration;
import cn.jieyuc.file.domain.UploadPurpose;
import cn.jieyuc.file.infrastructure.persistence.entity.CloudFileEntity;
import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 云文件聚合与持久化实体转换器。 */
@Mapper(config = DomainMappingConfig.class)
public interface CloudFilePersistenceConverter {
    /** 转换为数据库实体。 */
    @Mapping(target = "id", expression = "java(file.id().value())")
    @Mapping(target = "businessContext", expression = "java(file.ownership().contextCode())")
    @Mapping(target = "businessObjectType", expression = "java(file.ownership().objectType())")
    @Mapping(target = "businessObjectId", expression = "java(file.ownership().objectId())")
    @Mapping(target = "uploadPurpose", expression = "java(file.purpose().name())")
    @Mapping(target = "originalFileName", expression = "java(file.declaration().originalFileName())")
    @Mapping(target = "mediaType", expression = "java(file.declaration().mediaType())")
    @Mapping(target = "fileSize", expression = "java(file.declaration().size())")
    @Mapping(target = "sha256Digest", expression = "java(file.declaration().sha256())")
    @Mapping(target = "storageProvider", expression = "java(file.storageLocation().providerCode().name())")
    @Mapping(target = "storageBucket", expression = "java(file.storageLocation().bucket())")
    @Mapping(target = "storageObjectKey", expression = "java(file.storageLocation().objectKey())")
    @Mapping(target = "uploaderId", expression = "java(file.uploaderId())")
    @Mapping(target = "status", expression = "java(file.status().name())")
    @Mapping(target = "createdAt", expression = "java(file.createdAt())")
    @Mapping(target = "availableAt", expression = "java(file.availableAt())")
    @Mapping(target = "deletedAt", expression = "java(file.deletedAt())")
    @Mapping(target = "purgedAt", expression = "java(file.purgedAt())")
    @Mapping(target = "version", expression = "java(file.version())")
    CloudFileEntity toEntity(CloudFile file);

    /** 从可信数据库行重建聚合。 */
    default CloudFile toDomain(CloudFileEntity entity) {
        return new CloudFile(new CloudFileId(entity.getId()), entity.getUploaderId(),
                new BusinessOwnership(entity.getBusinessContext(), entity.getBusinessObjectType(),
                        entity.getBusinessObjectId()), UploadPurpose.valueOf(entity.getUploadPurpose()),
                new UploadDeclaration(entity.getOriginalFileName(), entity.getMediaType(), entity.getFileSize(),
                        entity.getSha256Digest()),
                new StorageLocation(StorageProviderCode.valueOf(entity.getStorageProvider()),
                        entity.getStorageBucket(), entity.getStorageObjectKey()),
                CloudFileStatus.valueOf(entity.getStatus()), entity.getCreatedAt(), entity.getAvailableAt(),
                entity.getDeletedAt(), entity.getPurgedAt(), entity.getVersion());
    }
}
