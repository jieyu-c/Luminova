package cn.jieyuc.luminovaauth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 找回密码 HTTP 请求。
 *
 * <p>验证码与新密码属于敏感凭证。只生成 Getter/Setter，禁止 {@code toString()}。</p>
 */
@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank
    private String requestId;
    @NotBlank
    private String phone;
    @NotBlank
    private String code;
    @NotBlank
    private String newPassword;
}
