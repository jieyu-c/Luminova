package cn.jieyuc.user.infrastructure.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.jieyuc.user.domain.model.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class UserSessionInvalidator {

    public void invalidateAllSessions(UserId userId) {
        if (userId == null) {
            return;
        }
        StpUtil.logout(userId.value());
    }
}
