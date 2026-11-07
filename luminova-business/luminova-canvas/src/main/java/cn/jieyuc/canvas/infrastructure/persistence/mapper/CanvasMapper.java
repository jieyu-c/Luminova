package cn.jieyuc.canvas.infrastructure.persistence.mapper;

import cn.jieyuc.canvas.infrastructure.persistence.entity.CanvasEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 画布类型数据库 Mapper。 */
@Mapper
public interface CanvasMapper extends BaseMapper<CanvasEntity> {
}
