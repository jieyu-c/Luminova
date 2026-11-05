package cn.jieyuc.user.domain.account;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 用户主体聚合根。
 *
 * <p>聚合维护账号状态、角色、资料和登录身份集合的不变量，不负责持久化、密码哈希、
 * 短信发送或会话签发。所有业务数据应引用稳定的 {@link UserId}。</p>
 */
public final class UserAccount {
    private final UserId id;
    private String nickname;
    private String avatarUrl;
    private Role role;
    private AccountStatus status;
    private OffsetDateTime lastLoginAt;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int version;
    private final List<LoginIdentity> identities;

    public UserAccount(UserId id, String nickname, String avatarUrl, Role role,
                       AccountStatus status, OffsetDateTime lastLoginAt,
                       OffsetDateTime createdAt, OffsetDateTime updatedAt,
                       int version, List<LoginIdentity> identities) {
        this.id = Objects.requireNonNull(id, "用户 ID 不能为空");
        this.nickname = validateNickname(nickname);
        this.avatarUrl = avatarUrl;
        this.role = Objects.requireNonNull(role, "角色不能为空");
        this.status = Objects.requireNonNull(status, "账号状态不能为空");
        this.lastLoginAt = lastLoginAt;
        this.createdAt = Objects.requireNonNull(createdAt, "创建时间不能为空");
        this.updatedAt = Objects.requireNonNull(updatedAt, "更新时间不能为空");
        this.version = version;
        this.identities = new ArrayList<>(Objects.requireNonNull(identities, "登录身份不能为空"));
        if (this.identities.isEmpty()) {
            throw new IllegalArgumentException("用户主体至少需要一个登录身份");
        }
    }

    /**
     * 创建拥有已验证手机号和密码凭证的普通用户。
     */
    public static UserAccount register(UserId userId, long identityId, PhoneNumber phone,
                                       PasswordCredential credential, OffsetDateTime now) {
        LoginIdentity identity = new LoginIdentity(
                identityId,
                new IdentityKey(IdentityType.PHONE, "LOCAL", phone.value()),
                phone.masked(),
                now,
                credential
        );
        String local = phone.value().substring(3);
        return new UserAccount(userId, "用户" + local.substring(7), null,
                Role.USER, AccountStatus.ACTIVE, null, now, now, 0, List.of(identity));
    }

    /**
     * 查找聚合内的指定登录身份。
     *
     * @param key 身份唯一键
     * @return 登录身份
     * @throws IllegalStateException 身份不属于当前用户主体
     */
    public LoginIdentity requireIdentity(IdentityKey key) {
        return identities.stream()
                .filter(identity -> identity.key().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("登录身份不存在"));
    }

    /**
     * 标记一次成功登录。
     *
     * <p>只有可用账号才能成功登录；认证上下文不能绕过该状态不变量。</p>
     */
    public void recordSuccessfulLogin(OffsetDateTime now) {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("账号不可用");
        }
        this.lastLoginAt = Objects.requireNonNull(now, "登录时间不能为空");
        this.updatedAt = now;
    }

    /**
     * 更新用户可自行维护的资料。
     */
    public void updateProfile(String nickname, String avatarUrl, OffsetDateTime now) {
        this.nickname = validateNickname(nickname);
        if (avatarUrl != null && (!avatarUrl.startsWith("https://") || avatarUrl.length() > 500)) {
            throw new IllegalArgumentException("头像地址必须是长度不超过 500 的 HTTPS URL");
        }
        this.avatarUrl = avatarUrl;
        this.updatedAt = Objects.requireNonNull(now, "更新时间不能为空");
    }

    /**
     * 禁用账号。
     *
     * <p>本方法只改变聚合状态。撤销会话属于聚合外副作用，必须在事务成功提交后执行。</p>
     */
    public void disable(UserId operatorId, OffsetDateTime now) {
        if (id.equals(operatorId)) {
            throw new IllegalArgumentException("管理员不能禁用自己");
        }
        this.status = AccountStatus.DISABLED;
        this.updatedAt = Objects.requireNonNull(now, "更新时间不能为空");
    }

    /**
     * 恢复账号使用。
     */
    public void enable(OffsetDateTime now) {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = Objects.requireNonNull(now, "更新时间不能为空");
    }

    /**
     * 将用户主体设置为管理员角色。
     *
     * <p>该行为只允许部署初始化用例调用，MVP 不通过 HTTP 暴露角色提升能力。</p>
     */
    public void grantAdminRole(OffsetDateTime now) {
        this.role = Role.ADMIN;
        this.updatedAt = Objects.requireNonNull(now, "更新时间不能为空");
    }

    private static String validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty() || nickname.trim().length() > 30) {
            throw new IllegalArgumentException("昵称长度必须为 1 到 30 个字符");
        }
        return nickname.trim();
    }

    public UserId id() { return id; }
    public String nickname() { return nickname; }
    public String avatarUrl() { return avatarUrl; }
    public Role role() { return role; }
    public AccountStatus status() { return status; }
    public OffsetDateTime lastLoginAt() { return lastLoginAt; }
    public OffsetDateTime createdAt() { return createdAt; }
    public OffsetDateTime updatedAt() { return updatedAt; }
    public int version() { return version; }
    public List<LoginIdentity> identities() { return Collections.unmodifiableList(identities); }
}
