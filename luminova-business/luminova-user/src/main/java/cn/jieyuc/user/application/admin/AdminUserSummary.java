package cn.jieyuc.user.application.admin;

import java.time.OffsetDateTime;

/**
 * 管理员用户列表中的脱敏摘要。
 */
public record AdminUserSummary(long userId, String maskedPhone, String nickname,
                               String role, String status, OffsetDateTime createdAt) {
}
