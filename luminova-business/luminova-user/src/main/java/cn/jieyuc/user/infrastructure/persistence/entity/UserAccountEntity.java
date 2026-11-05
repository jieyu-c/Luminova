package cn.jieyuc.user.infrastructure.persistence.entity;

import java.time.OffsetDateTime;

/**
 * user_account 表的持久化 Entity。
 *
 * <p>该对象只在基础设施层使用，不得直接返回给 Controller 或跨上下文接口。</p>
 */
public class UserAccountEntity {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String role;
    private String status;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(OffsetDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
