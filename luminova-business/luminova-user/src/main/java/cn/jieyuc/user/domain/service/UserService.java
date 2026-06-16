package cn.jieyuc.user.domain.service;

import cn.jieyuc.user.pojo.request.LoginRequest;
import cn.jieyuc.user.pojo.response.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequest request);
}
