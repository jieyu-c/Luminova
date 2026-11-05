package cn.jieyuc.user.domain.account;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 用户主体拥有的登录身份实体。
 *
 * <p>实体表达身份归属和验证状态，不执行密码校验，也不调用外部身份提供方。</p>
 */
public final class LoginIdentity {
    private final long id;
    private final IdentityKey key;
    private final String maskedIdentifier;
    private final OffsetDateTime verifiedAt;
    private PasswordCredential passwordCredential;

    public LoginIdentity(long id, IdentityKey key, String maskedIdentifier,
                         OffsetDateTime verifiedAt, PasswordCredential passwordCredential) {
        if (id <= 0) {
            throw new IllegalArgumentException("登录身份 ID 必须为正数");
        }
        this.id = id;
        this.key = Objects.requireNonNull(key, "身份键不能为空");
        this.maskedIdentifier = Objects.requireNonNull(maskedIdentifier, "脱敏标识不能为空");
        this.verifiedAt = Objects.requireNonNull(verifiedAt, "身份验证时间不能为空");
        this.passwordCredential = passwordCredential;
    }

    public long id() { return id; }
    public IdentityKey key() { return key; }
    public String maskedIdentifier() { return maskedIdentifier; }
    public OffsetDateTime verifiedAt() { return verifiedAt; }
    public PasswordCredential passwordCredential() { return passwordCredential; }

    /**
     * 替换本地密码凭证。
     *
     * @param credential 已完成策略校验和哈希的新凭证
     */
    public void replacePassword(PasswordCredential credential) {
        this.passwordCredential = Objects.requireNonNull(credential, "密码凭证不能为空");
    }
}
