package cn.jieyuc.user.infrastructure.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.jieyuc.user.domain.model.user.UserId;

public final class LoginUserHolder {

    private LoginUserHolder() {
    }

    public static UserId requireLoginUserId() {
        StpUtil.checkLogin();
        return new UserId(StpUtil.getLoginIdAsLong());
    }
}
