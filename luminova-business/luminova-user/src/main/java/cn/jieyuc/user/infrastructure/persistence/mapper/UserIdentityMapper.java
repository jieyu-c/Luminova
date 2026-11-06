package cn.jieyuc.user.infrastructure.persistence.mapper;

import cn.jieyuc.user.infrastructure.persistence.entity.LoginIdentityEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * user_identity 表的 MyBatis-Plus Mapper。
 *
 * <p>身份唯一键查询和按用户加载身份都通过 Wrapper 完成，避免在 Mapper 中拼接业务条件。</p>
 */
@Mapper
public interface UserIdentityMapper extends BaseMapper<LoginIdentityEntity> {
}
