package cn.jieyuc.user.application.service;

import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.domain.service.RegisterVerifyCodeSender;

public interface UserApplicationService {
    RegisterVerifyCodeSender.SendResult sendRegisterVerifyCode(String phone);

    User register(String phone, String password, String verifyCode);

    User queryById(UserId userId);

    User updateProfile(UserId userId, String username, String nickname);

    User updateUsername(UserId userId, String username);

    User updateAvatar(UserId userId, String avatarUrl);

    void changePassword(UserId userId, String oldPassword, String newPassword);
}
