package cn.jieyuc.user.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录后修改密码 HTTP 请求。
 *
 * <p>两个字段均为敏感凭证。只生成 Getter/Setter，禁止 {@code toString()}。</p>
 */
@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
}
