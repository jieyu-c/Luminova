package cn.jieyuc.luminovaauth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 手机号注册 HTTP 请求。
 *
 * <p>code 和 password 均为敏感凭证，本类禁止生成 {@code toString()}。</p>
 */
public class RegisterRequest {
    @NotBlank private String requestId;
    @NotBlank private String phone;
    @NotBlank private String code;
    @NotBlank private String password;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
