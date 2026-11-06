package cn.jieyuc.user.infrastructure.persistence.mapper;

import cn.jieyuc.user.infrastructure.persistence.entity.PasswordCredentialEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * password_credential 表的 MyBatis-Plus Mapper。
 *
 * <p>以 identity_id 为主键做单行读写，不在此处比较密码。</p>
 */
@Mapper
public interface PasswordCredentialMapper extends BaseMapper<PasswordCredentialEntity> {
}
