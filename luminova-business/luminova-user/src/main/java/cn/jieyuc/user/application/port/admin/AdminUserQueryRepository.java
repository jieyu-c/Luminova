package cn.jieyuc.user.application.port.admin;

import cn.jieyuc.user.application.admin.AdminUserPage;

/**
 * 管理员用户只读查询端口。
 *
 * <p>复杂分页查询不加载完整聚合，但所有返回标识必须脱敏。</p>
 */
public interface AdminUserQueryRepository {

    /** 执行分页、状态和精确身份查询。 */
    AdminUserPage query(int page, int size, String status, Long userId, String normalizedPhone);
}
