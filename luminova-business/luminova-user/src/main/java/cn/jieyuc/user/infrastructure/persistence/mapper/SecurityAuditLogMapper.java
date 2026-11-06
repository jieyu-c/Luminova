package cn.jieyuc.user.infrastructure.persistence.mapper;

import cn.jieyuc.user.infrastructure.persistence.entity.SecurityAuditLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * security_audit_log 表的 MyBatis-Plus Mapper。
 *
 * <p>审计只追加。更新和删除不在仓储中暴露。</p>
 */
@Mapper
public interface SecurityAuditLogMapper extends BaseMapper<SecurityAuditLogEntity> {
}
