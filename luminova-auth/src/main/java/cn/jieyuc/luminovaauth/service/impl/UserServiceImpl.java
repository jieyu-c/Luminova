package cn.jieyuc.luminovaauth.service.impl;

import cn.jieyuc.luminovaauth.pojo.request.LoginRequest;
import cn.jieyuc.luminovaauth.pojo.response.LoginResponse;
import cn.jieyuc.luminovaauth.service.UserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    @Override
    public LoginResponse login(LoginRequest request) {
        return new LoginResponse();
    }
}
