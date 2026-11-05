package cn.jieyuc.luminovaauth.interfaces.dto;

import cn.jieyuc.luminovaauth.domain.verification.VerificationPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 发送验证码 HTTP 请求。
 */
@Getter
@Setter
public class SendVerificationCodeRequest {
    @NotBlank
    private String phone;
    @NotNull
    private VerificationPurpose purpose;
}
