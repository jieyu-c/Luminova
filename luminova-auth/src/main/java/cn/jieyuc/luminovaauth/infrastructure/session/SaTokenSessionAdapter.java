package cn.jieyuc.luminovaauth.infrastructure.session;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.jieyuc.luminovaauth.application.login.AuthenticatedPrincipal;
import cn.jieyuc.luminovaauth.application.session.IssuedSession;
import cn.jieyuc.luminovaauth.application.session.SessionIssuer;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 会话 Adapter。
 *
 * <p>会话登录 ID 固定使用稳定 userId。Token 最长存活七天，两小时无操作后失效；
 * 具体 Redis 数据结构不会泄漏给应用层。</p>
 */
@Component
public class SaTokenSessionAdapter implements SessionIssuer {
    private static final long MAX_TIMEOUT_SECONDS = 7 * 24 * 60 * 60L;
    private static final long ACTIVE_TIMEOUT_SECONDS = 2 * 60 * 60L;

    @Override
    public IssuedSession issue(AuthenticatedPrincipal principal) {
        SaLoginModel model = new SaLoginModel()
                .setTimeout(MAX_TIMEOUT_SECONDS)
                .setActiveTimeout(ACTIVE_TIMEOUT_SECONDS)
                .setExtra("role", principal.role())
                .setExtra("identityId", principal.identityId());
        StpUtil.login(principal.userId(), model);
        return new IssuedSession(StpUtil.getTokenValue(), StpUtil.getTokenTimeout());
    }

    @Override
    public void revokeCurrent() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Override
    public void revokeAll(long userId) {
        StpUtil.logout(userId);
    }
}
