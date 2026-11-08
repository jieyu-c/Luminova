package cn.jieyuc.file.infrastructure.persistence.mapper;

import cn.jieyuc.file.infrastructure.persistence.entity.CloudFileEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 云文件单表 Mapper。 */
@Mapper
public interface CloudFileMapper extends BaseMapper<CloudFileEntity> {
}
