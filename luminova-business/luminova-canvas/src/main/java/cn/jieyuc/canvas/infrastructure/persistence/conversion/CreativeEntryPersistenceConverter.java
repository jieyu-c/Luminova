package cn.jieyuc.canvas.infrastructure.persistence.conversion;

import cn.jieyuc.canvas.domain.workspace.CreativeEntry;
import cn.jieyuc.canvas.infrastructure.persistence.entity.CreativeEntryEntity;
import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 创作条目聚合与持久化实体转换器。 */
@Mapper(config = DomainMappingConfig.class)
public interface CreativeEntryPersistenceConverter {
    @Mapping(target = "parentEntryId", expression = "java(entry.parentId())")
    @Mapping(target = "entryType", expression = "java(entry.type().name())")
    @Mapping(target = "id", expression = "java(entry.id())")
    @Mapping(target = "ownerId", expression = "java(entry.ownerId())")
    @Mapping(target = "name", expression = "java(entry.name())")
    @Mapping(target = "sortOrder", expression = "java(entry.sortOrder())")
    @Mapping(target = "customCoverId", expression = "java(entry.customCoverId())")
    @Mapping(target = "createdAt", expression = "java(entry.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(entry.updatedAt())")
    @Mapping(target = "deletedAt", expression = "java(entry.deletedAt())")
    @Mapping(target = "version", expression = "java(entry.version())")
    CreativeEntryEntity toEntity(CreativeEntry entry);

    /** 从可信数据库行重建聚合。 */
    default CreativeEntry toDomain(CreativeEntryEntity entity) {
        return new CreativeEntry(entity.getId(), entity.getOwnerId(),
                cn.jieyuc.canvas.domain.workspace.EntryType.valueOf(entity.getEntryType()), entity.getName(),
                entity.getParentEntryId(), entity.getSortOrder(), entity.getCustomCoverId(),
                entity.getCreatedAt(), entity.getUpdatedAt(), entity.getDeletedAt(), entity.getVersion());
    }
}
