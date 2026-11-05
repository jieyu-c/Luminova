package cn.jieyuc.user.application.account;

import java.time.OffsetDateTime;

/**
 * 用户本人可查看的资料视图。
 */
public record UserProfileView(long id, String maskedPhone, String nickname, String avatarUrl,
                              String role, String status, OffsetDateTime createdAt) {
}
