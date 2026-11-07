package cn.jieyuc.canvas.infrastructure.persistence.mapper;

import cn.jieyuc.canvas.infrastructure.persistence.entity.RecentlyOpenedCanvasEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 最近打开数据库 Mapper。 */
@Mapper
public interface RecentlyOpenedCanvasMapper extends BaseMapper<RecentlyOpenedCanvasEntity> {
}
