package cn.jieyuc.file.infrastructure.persistence.mapper;

import cn.jieyuc.file.infrastructure.persistence.entity.FileReferenceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 文件引用账本单表 Mapper。 */
@Mapper
public interface FileReferenceMapper extends BaseMapper<FileReferenceEntity> {
}
