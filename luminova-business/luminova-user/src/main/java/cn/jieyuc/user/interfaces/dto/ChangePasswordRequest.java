package cn.jieyuc.user.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录后修改密码 HTTP 请求。
 *
 * <p>两个字段均为敏感凭证，本类禁止生成 {@code toString()}。</p>
 */
public class ChangePasswordRequest {
    @NotBlank private String currentPassword;
    @NotBlank private String newPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
