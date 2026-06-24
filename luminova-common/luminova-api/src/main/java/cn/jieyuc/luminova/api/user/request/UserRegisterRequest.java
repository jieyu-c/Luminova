package cn.jieyuc.luminova.api.user.request;

import cn.jieyuc.luminova.base.request.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest extends BaseRequest {
    private String phone;
    private String password;
}
