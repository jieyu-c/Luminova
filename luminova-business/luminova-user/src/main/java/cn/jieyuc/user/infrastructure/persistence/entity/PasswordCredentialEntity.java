package cn.jieyuc.user.infrastructure.persistence.entity;

import java.time.OffsetDateTime;

/**
 * password_credential 表的持久化 Entity。
 *
 * <p>包含密码哈希，禁止生成 {@code toString()} 或写入日志。</p>
 */
public class PasswordCredentialEntity {
    private Long identityId;
    private String passwordHash;
    private OffsetDateTime passwordChangedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getIdentityId() { return identityId; }
    public void setIdentityId(Long identityId) { this.identityId = identityId; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public OffsetDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(OffsetDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
