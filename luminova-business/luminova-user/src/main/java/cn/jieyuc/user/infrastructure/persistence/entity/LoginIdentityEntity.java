package cn.jieyuc.user.infrastructure.persistence.entity;

import java.time.OffsetDateTime;

/**
 * user_identity 表的持久化 Entity。
 *
 * <p>identifier 含完整登录标识，禁止输出该对象的字符串形式。</p>
 */
public class LoginIdentityEntity {
    private Long id;
    private Long userId;
    private String identityType;
    private String provider;
    private String identifier;
    private String identifierMasked;
    private OffsetDateTime verifiedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getIdentityType() { return identityType; }
    public void setIdentityType(String identityType) { this.identityType = identityType; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getIdentifierMasked() { return identifierMasked; }
    public void setIdentifierMasked(String identifierMasked) { this.identifierMasked = identifierMasked; }
    public OffsetDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(OffsetDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
