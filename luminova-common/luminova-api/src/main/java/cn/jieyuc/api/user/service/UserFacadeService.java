package cn.jieyuc.api.user.service;

import cn.jieyuc.api.user.request.UserRegisterRequest;
import cn.jieyuc.api.user.response.UserOperateResponse;

public interface UserFacadeService {
    /**
     * 用户注册
     *
     * @param request
     * @return
     */
    UserOperateResponse register(UserRegisterRequest request);
}
