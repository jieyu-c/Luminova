package cn.jieyuc.user.infrastructure.persistence.mapper;

import cn.jieyuc.user.infrastructure.persistence.entity.UserAccountEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * user_account 表的 MyBatis-Plus Mapper。
 *
 * <p>只继承通用单表能力。查询条件由仓储 Adapter 用 {@code LambdaQueryWrapper} 组装，
 * 本接口不写 SQL，也不判断账号是否允许登录。</p>
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccountEntity> {
}
