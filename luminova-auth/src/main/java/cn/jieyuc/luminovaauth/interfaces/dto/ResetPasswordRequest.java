package cn.jieyuc.luminovaauth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 找回密码 HTTP 请求。
 *
 * <p>验证码与新密码属于敏感凭证，本类禁止生成 {@code toString()}。</p>
 */
public class ResetPasswordRequest {
    @NotBlank private String requestId;
    @NotBlank private String phone;
    @NotBlank private String code;
    @NotBlank private String newPassword;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
