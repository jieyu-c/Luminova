package cn.jieyuc.user.infrastructure.persistence;

import cn.jieyuc.user.application.admin.AdminUserPage;
import cn.jieyuc.user.application.admin.AdminUserQueryRepository;
import cn.jieyuc.user.application.admin.AdminUserSummary;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserAccountMapper;
import org.springframework.stereotype.Repository;

/**
 * 管理员用户分页查询 MyBatis Adapter。
 *
 * <p>使用只读投影避免加载完整聚合，且 SQL 只返回脱敏手机号。</p>
 */
@Repository
public class MybatisAdminUserQueryRepository implements AdminUserQueryRepository {
    private final UserAccountMapper mapper;

    public MybatisAdminUserQueryRepository(UserAccountMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AdminUserPage query(int page, int size, String status, Long userId, String normalizedPhone) {
        int offset = (page - 1) * size;
        var items = mapper.queryAdminUsers(status, userId, normalizedPhone, size, offset).stream()
                .map(row -> new AdminUserSummary(row.getUserId(), row.getMaskedPhone(),
                        row.getNickname(), row.getRole(), row.getStatus(), row.getCreatedAt()))
                .toList();
        long total = mapper.countAdminUsers(status, userId, normalizedPhone);
        return new AdminUserPage(items, total, page, size);
    }
}
