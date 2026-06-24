package cn.jieyuc.user.interfaces.dto.response;

import cn.jieyuc.luminova.base.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SendVerifyCodeResponse extends BaseResponse {

    private Integer expiresInSeconds;

    private String verifyCode;

    public static SendVerifyCodeResponse of(int expiresInSeconds, String verifyCode) {
        SendVerifyCodeResponse resp = new SendVerifyCodeResponse();
        resp.setSuccess(true);
        resp.setExpiresInSeconds(expiresInSeconds);
        resp.setVerifyCode(verifyCode);
        return resp;
    }

    public static SendVerifyCodeResponse of(int expiresInSeconds) {
        SendVerifyCodeResponse resp = new SendVerifyCodeResponse();
        resp.setSuccess(true);
        resp.setExpiresInSeconds(expiresInSeconds);
        return resp;
    }
}
