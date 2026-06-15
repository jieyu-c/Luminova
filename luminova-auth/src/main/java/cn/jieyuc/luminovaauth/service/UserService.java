package cn.jieyuc.luminovaauth.service;

import cn.jieyuc.luminovaauth.pojo.request.LoginRequest;
import cn.jieyuc.luminovaauth.pojo.response.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequest request);
}
