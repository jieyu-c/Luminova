package cn.jieyuc.user.domain.service;

import cn.jieyuc.user.domain.model.user.Phone;

public interface RegisterVerifyCodeVerifier {
    void verify(Phone phone, String verifyCode);
}
