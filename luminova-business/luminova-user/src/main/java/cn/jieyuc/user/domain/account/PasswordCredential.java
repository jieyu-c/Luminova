package cn.jieyuc.user.domain.account;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 本地密码凭证。
 *
 * <p>对象只持有不可逆哈希，不接收 HTTP DTO，也不决定哈希算法。哈希值不得通过
 * {@code toString()}、接口响应或异常信息泄漏。</p>
 */
public final class PasswordCredential {
    private final String passwordHash;
    private final OffsetDateTime changedAt;

    public PasswordCredential(String passwordHash, OffsetDateTime changedAt) {
        this.passwordHash = Objects.requireNonNull(passwordHash, "密码哈希不能为空");
        this.changedAt = Objects.requireNonNull(changedAt, "密码变更时间不能为空");
    }

    /**
     * 返回仅供领域密码校验 seam 使用的哈希。
     */
    public String passwordHash() {
        return passwordHash;
    }

    /**
     * 返回最近密码变更时间。
     */
    public OffsetDateTime changedAt() {
        return changedAt;
    }

    /**
     * 使用新的不可逆哈希替换凭证。
     *
     * @param newHash 新密码哈希
     * @param now     变更时间
     * @return 新凭证
     */
    public PasswordCredential replace(String newHash, OffsetDateTime now) {
        return new PasswordCredential(newHash, now);
    }
}
