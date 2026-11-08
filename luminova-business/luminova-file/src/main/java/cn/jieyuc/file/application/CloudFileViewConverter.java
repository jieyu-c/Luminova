package cn.jieyuc.file.application;

import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.luminova.base.mapping.DomainMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 云文件聚合到公开视图的转换器。 */
@Mapper(config = DomainMappingConfig.class)
public interface CloudFileViewConverter {
    /** 转换为不泄漏存储位置的公开视图。 */
    @Mapping(target = "fileId", expression = "java(file.id().value())")
    @Mapping(target = "originalFileName", expression = "java(file.declaration().originalFileName())")
    @Mapping(target = "mediaType", expression = "java(file.declaration().mediaType())")
    @Mapping(target = "size", expression = "java(file.declaration().size())")
    @Mapping(target = "sha256", expression = "java(file.declaration().sha256())")
    @Mapping(target = "purpose", expression = "java(file.purpose())")
    @Mapping(target = "status", expression = "java(file.status())")
    @Mapping(target = "createdAt", expression = "java(file.createdAt())")
    @Mapping(target = "availableAt", expression = "java(file.availableAt())")
    CloudFileView toView(CloudFile file);
}
