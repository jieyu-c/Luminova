package cn.jieyuc.user.infrastructure.persistence.entity;

import java.time.OffsetDateTime;

/**
 * 管理员分页查询的只读投影。
 *
 * <p>投影只包含脱敏手机号，不得增加完整 identifier 字段。</p>
 */
public class AdminUserRow {
    private Long userId;
    private String maskedPhone;
    private String nickname;
    private String role;
    private String status;
    private OffsetDateTime createdAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMaskedPhone() { return maskedPhone; }
    public void setMaskedPhone(String maskedPhone) { this.maskedPhone = maskedPhone; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
