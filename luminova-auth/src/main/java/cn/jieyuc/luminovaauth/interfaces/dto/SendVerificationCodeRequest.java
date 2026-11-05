package cn.jieyuc.luminovaauth.interfaces.dto;

import cn.jieyuc.luminovaauth.domain.verification.VerificationPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送验证码 HTTP 请求。
 */
public class SendVerificationCodeRequest {
    @NotBlank
    private String phone;
    @NotNull
    private VerificationPurpose purpose;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public VerificationPurpose getPurpose() { return purpose; }
    public void setPurpose(VerificationPurpose purpose) { this.purpose = purpose; }
}
