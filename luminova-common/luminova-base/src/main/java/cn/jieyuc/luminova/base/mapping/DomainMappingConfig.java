package cn.jieyuc.luminova.base.mapping;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * 领域类型与外层类型之间的 MapStruct 配置。
 *
 * <p>转换器只出现在 application 或 infrastructure，类名以 Converter 结尾。
 * domain 不依赖 MapStruct，这样聚合仍只能通过自己的构造器和行为方法建立。
 * 未映射的目标属性直接编译失败，避免字段被静默丢掉。</p>
 */
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DomainMappingConfig {
}
