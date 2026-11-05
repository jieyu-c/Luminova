package cn.jieyuc.luminovaauth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 手机号密码登录 HTTP 请求。
 *
 * <p>password 为敏感字段。只生成 Getter/Setter，刻意不生成 {@code toString()}。</p>
 */
@Getter
@Setter
public class PhonePasswordLoginRequest {
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
}
