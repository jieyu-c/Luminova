package cn.jieyuc.generation.infrastructure.persistence.mapper;

import cn.jieyuc.generation.infrastructure.persistence.entity.GenerationTaskPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GenerationTaskMapper extends BaseMapper<GenerationTaskPO> {
}
