package cn.jieyuc.luminovaauth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 手机号注册 HTTP 请求。
 *
 * <p>code 和 password 均为敏感凭证。只生成 Getter/Setter，禁止 {@code toString()}。</p>
 */
@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    private String requestId;
    @NotBlank
    private String phone;
    @NotBlank
    private String code;
    @NotBlank
    private String password;
}
