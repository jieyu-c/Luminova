package cn.jieyuc.user.infrastructure.session;

import cn.dev33.satoken.stp.StpUtil;
import cn.jieyuc.user.application.port.AuthenticatedUserProvider;
import cn.jieyuc.user.application.port.UserSessionRevoker;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 当前用户与会话撤销 Adapter。
 *
 * <p>应用层只看到稳定 userId，不依赖 Sa-Token 静态接口。</p>
 */
@Component
public class SaTokenAuthenticatedUserAdapter implements AuthenticatedUserProvider, UserSessionRevoker {
    @Override
    public long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    @Override
    public void revokeAll(long userId) {
        StpUtil.logout(userId);
    }
}
