package cn.jieyuc.user.infrastructure.persistence;

import cn.jieyuc.user.application.admin.AdminUserPage;
import cn.jieyuc.user.application.admin.AdminUserSummary;
import cn.jieyuc.user.application.port.admin.AdminUserQueryRepository;
import cn.jieyuc.user.domain.account.IdentityKey;
import cn.jieyuc.user.domain.account.IdentityType;
import cn.jieyuc.user.infrastructure.persistence.entity.LoginIdentityEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.UserAccountEntity;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserAccountMapper;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserIdentityMapper;
import cn.jieyuc.user.infrastructure.persistence.conversion.AdminUserSummaryPersistenceConverter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 管理员用户分页查询仓储。
 *
 * <p>分页走 MyBatis-Plus 的 {@code Page}，由分页插件追加 limit。手机号条件先查唯一身份，
 * 再按用户主体分页，因此同一用户不会因为多条身份被重复列出。投影只保留脱敏手机号。</p>
 */
@Repository
public class AdminUserQueryRepositoryAdapter implements AdminUserQueryRepository {
    private final UserAccountMapper accountMapper;
    private final UserIdentityMapper identityMapper;
    private final AdminUserSummaryPersistenceConverter summaryConverter;

    public AdminUserQueryRepositoryAdapter(UserAccountMapper accountMapper, UserIdentityMapper identityMapper,
                                           AdminUserSummaryPersistenceConverter summaryConverter) {
        this.accountMapper = accountMapper;
        this.identityMapper = identityMapper;
        this.summaryConverter = summaryConverter;
    }

    @Override
    public AdminUserPage query(int page, int size, String status, Long userId, String normalizedPhone) {
        Long scopedUserId = resolveUserId(userId, normalizedPhone);
        if (hasText(normalizedPhone) && scopedUserId == null) {
            return new AdminUserPage(List.of(), 0, page, size);
        }
        LambdaQueryWrapper<UserAccountEntity> query = Wrappers.<UserAccountEntity>lambdaQuery()
                .eq(hasText(status), UserAccountEntity::getStatus, status)
                .eq(scopedUserId != null, UserAccountEntity::getId, scopedUserId)
                .orderByDesc(UserAccountEntity::getCreatedAt)
                .orderByDesc(UserAccountEntity::getId);
        Page<UserAccountEntity> result = accountMapper.selectPage(new Page<>(page, size), query);
        Map<Long, String> maskedPhones = maskedPhones(result.getRecords());
        List<AdminUserSummary> items = result.getRecords().stream()
                .map(account -> summaryConverter.toSummary(account, maskedPhones.get(account.getId())))
                .toList();
        return new AdminUserPage(items, result.getTotal(), page, size);
    }

    /**
     * 手机号过滤必须同时满足指定用户 ID。身份不存在或属于其他用户时返回 null，调用方据此得到空页。
     */
    private Long resolveUserId(Long userId, String normalizedPhone) {
        if (!hasText(normalizedPhone)) {
            return userId;
        }
        LoginIdentityEntity identity = identityMapper.selectOne(phoneIdentityQuery()
                .eq(LoginIdentityEntity::getIdentifier, normalizedPhone));
        if (identity == null || (userId != null && !userId.equals(identity.getUserId()))) {
            return null;
        }
        return identity.getUserId();
    }

    private Map<Long, String> maskedPhones(List<UserAccountEntity> accounts) {
        if (accounts.isEmpty()) {
            return Map.of();
        }
        List<Long> userIds = accounts.stream().map(UserAccountEntity::getId).toList();
        return identityMapper.selectList(phoneIdentityQuery()
                        .in(LoginIdentityEntity::getUserId, userIds)
                        .orderByAsc(LoginIdentityEntity::getId))
                .stream()
                .collect(Collectors.toMap(LoginIdentityEntity::getUserId,
                        LoginIdentityEntity::getIdentifierMasked, (first, ignored) -> first));
    }

    private LambdaQueryWrapper<LoginIdentityEntity> phoneIdentityQuery() {
        return Wrappers.<LoginIdentityEntity>lambdaQuery()
                .eq(LoginIdentityEntity::getIdentityType, IdentityType.PHONE.name())
                .eq(LoginIdentityEntity::getProvider, IdentityKey.LOCAL_PROVIDER);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
