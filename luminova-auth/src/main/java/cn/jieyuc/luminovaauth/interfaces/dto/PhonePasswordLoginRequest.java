package cn.jieyuc.luminovaauth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 手机号密码登录 HTTP 请求。
 *
 * <p>password 为敏感字段，本类刻意不实现 {@code toString()}。</p>
 */
public class PhonePasswordLoginRequest {
    @NotBlank
    private String phone;
    @NotBlank
    private String password;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
