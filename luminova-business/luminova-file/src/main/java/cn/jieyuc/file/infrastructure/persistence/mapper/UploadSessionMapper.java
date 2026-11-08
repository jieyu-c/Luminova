package cn.jieyuc.file.infrastructure.persistence.mapper;

import cn.jieyuc.file.infrastructure.persistence.entity.UploadSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 上传会话单表 Mapper。 */
@Mapper
public interface UploadSessionMapper extends BaseMapper<UploadSessionEntity> {
}
