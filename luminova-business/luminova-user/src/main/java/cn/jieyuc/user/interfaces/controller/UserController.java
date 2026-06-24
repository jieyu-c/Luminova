package cn.jieyuc.user.interfaces.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.lang.Assert;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminova.base.response.BaseResponse;
import cn.jieyuc.user.application.service.UserApplicationService;
import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.infrastructure.web.LoginUserHolder;
import cn.jieyuc.user.infrastructure.web.UserExceptionEnums;
import cn.jieyuc.user.interfaces.dto.request.ChangePasswordRequest;
import cn.jieyuc.user.domain.service.RegisterVerifyCodeSender;
import cn.jieyuc.user.interfaces.dto.request.RegisterRequest;
import cn.jieyuc.user.interfaces.dto.request.SendVerifyCodeRequest;
import cn.jieyuc.user.interfaces.dto.request.UpdateAvatarRequest;
import cn.jieyuc.user.interfaces.dto.request.UpdateUsernameRequest;
import cn.jieyuc.user.interfaces.dto.response.RegisterResponse;
import cn.jieyuc.user.interfaces.dto.response.SendVerifyCodeResponse;
import cn.jieyuc.user.interfaces.dto.response.UserInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserApplicationService userApplicationService;

    @PostMapping("/verify-code/send")
    public SendVerifyCodeResponse sendVerifyCode(@RequestBody SendVerifyCodeRequest request) {
        RegisterVerifyCodeSender.SendResult result = userApplicationService.sendRegisterVerifyCode(request.phone());
        if (result.verifyCode() != null) {
            return SendVerifyCodeResponse.of(result.expiresInSeconds(), result.verifyCode());
        }
        return SendVerifyCodeResponse.of(result.expiresInSeconds());
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        User user = userApplicationService.register(request.phone(), request.password(), request.verifyCode());
        return RegisterResponse.from(user);
    }

    @SaCheckLogin
    @GetMapping("/info")
    public UserInfoResponse getUserInfo() {
        UserId userId = LoginUserHolder.requireLoginUserId();
        User user = userApplicationService.queryById(userId);
        Assert.notNull(user, () -> new BizException(UserExceptionEnums.USER_NOT_EXIST.name(), "User not exist"));
        return UserInfoResponse.from(user);
    }

    @SaCheckLogin
    @PutMapping("/username")
    public UserInfoResponse updateUsername(@RequestBody UpdateUsernameRequest request) {
        UserId userId = LoginUserHolder.requireLoginUserId();
        User user = userApplicationService.updateUsername(userId, request.username());
        return UserInfoResponse.from(user);
    }

    @SaCheckLogin
    @PutMapping("/avatar")
    public UserInfoResponse updateAvatar(@RequestBody UpdateAvatarRequest request) {
        UserId userId = LoginUserHolder.requireLoginUserId();
        User user = userApplicationService.updateAvatar(userId, request.avatarUrl());
        return UserInfoResponse.from(user);
    }

    @SaCheckLogin
    @PutMapping("/password")
    public BaseResponse changePassword(@RequestBody ChangePasswordRequest request) {
        UserId userId = LoginUserHolder.requireLoginUserId();
        userApplicationService.changePassword(userId, request.oldPassword(), request.newPassword());
        return BaseResponse.success();
    }
}
