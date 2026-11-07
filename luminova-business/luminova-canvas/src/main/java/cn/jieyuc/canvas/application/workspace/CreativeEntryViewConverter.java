package cn.jieyuc.canvas.application.workspace;

import cn.jieyuc.canvas.domain.workspace.CreativeEntry;
import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 创作条目聚合到应用视图的转换器。 */
@Mapper(config = DomainMappingConfig.class)
public interface CreativeEntryViewConverter {
    /** 转换列表和详情需要的字段。 */
    @Mapping(target = "id", expression = "java(entry.id())")
    @Mapping(target = "type", expression = "java(entry.type())")
    @Mapping(target = "name", expression = "java(entry.name())")
    @Mapping(target = "parentId", expression = "java(entry.parentId())")
    @Mapping(target = "sortOrder", expression = "java(entry.sortOrder())")
    @Mapping(target = "customCoverId", expression = "java(entry.customCoverId())")
    @Mapping(target = "createdAt", expression = "java(entry.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(entry.updatedAt())")
    @Mapping(target = "version", expression = "java(entry.version())")
    CreativeEntryView toView(CreativeEntry entry);
}
