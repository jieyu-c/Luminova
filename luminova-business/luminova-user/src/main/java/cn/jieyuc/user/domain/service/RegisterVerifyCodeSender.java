package cn.jieyuc.user.domain.service;

import cn.jieyuc.user.domain.model.user.Phone;

public interface RegisterVerifyCodeSender {

    SendResult send(Phone phone);

    record SendResult(String verifyCode, int expiresInSeconds) {}
}
