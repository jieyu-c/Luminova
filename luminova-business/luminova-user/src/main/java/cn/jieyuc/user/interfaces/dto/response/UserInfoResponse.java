package cn.jieyuc.user.interfaces.dto.response;

import cn.jieyuc.luminova.base.response.BaseResponse;
import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.user.UserInfo;
import lombok.Data;

@Data
public class UserInfoResponse extends BaseResponse {
    private UserInfo userInfo;

    public static UserInfoResponse from(User user) {
        UserInfoResponse resp = new UserInfoResponse();
        resp.setSuccess(true);
        resp.setUserInfo(UserInfo.from(user));
        return resp;
    }
}
