package cn.jieyuc.luminova.base.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class BaseResponse {
    private Boolean success;
    private Error error;

    public static BaseResponse success() {
        BaseResponse response = new BaseResponse();
        response.setSuccess(true);
        return response;
    }

    public static BaseResponse error(String code, String msg) {
        BaseResponse response = new BaseResponse();
        response.setSuccess(false);
        response.setError(new Error(code, msg));
        return response;
    }
}
