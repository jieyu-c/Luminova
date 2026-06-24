package cn.jieyuc.user.interfaces.dto.response;

import cn.jieyuc.luminova.base.response.BaseResponse;
import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.user.UserInfo;
import lombok.Data;

@Data
public class RegisterResponse extends BaseResponse {
    private UserInfo userInfo;

    public static RegisterResponse from(User user) {
        RegisterResponse resp = new RegisterResponse();
        resp.setSuccess(true);
        resp.setUserInfo(UserInfo.from(user));
        return resp;
    }
}
