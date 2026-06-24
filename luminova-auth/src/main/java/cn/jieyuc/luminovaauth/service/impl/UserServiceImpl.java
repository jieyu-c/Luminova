package cn.jieyuc.luminovaauth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminova.api.user.response.data.UserInfo;
import cn.jieyuc.luminova.api.user.service.UserFacadeService;
import cn.jieyuc.luminovaauth.pojo.request.LoginRequest;
import cn.jieyuc.luminovaauth.pojo.response.LoginResponse;
import cn.jieyuc.luminovaauth.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    @DubboReference(version = "v1.0.0", timeout = 5000)
    private UserFacadeService userFacadeService;

    @Override
    public LoginResponse login(LoginRequest request) {
        Assert.notNull(request, () -> new BizException("LOGIN_REQUEST_EMPTY", "Login request is empty"));
        UserInfo userInfo = userFacadeService.authenticate(request.phone(), request.password());
        Assert.notNull(userInfo, () -> new BizException("LOGIN_FAILED", "手机号或密码错误"));

        StpUtil.login(userInfo.getUserId());
        String token = StpUtil.getTokenValue();
        return new LoginResponse(token, userInfo.getUserId(), userInfo.getUsername(), userInfo.getAvatarUrl());
    }
}
