package cn.jieyuc.canvas.infrastructure.session;

import cn.dev33.satoken.stp.StpUtil;
import cn.jieyuc.canvas.application.port.session.AuthenticatedCreatorProvider;
import org.springframework.stereotype.Component;

/** 将 Sa-Token 会话适配为稳定用户主体 ID。 */
@Component
public class SaTokenAuthenticatedCreatorAdapter implements AuthenticatedCreatorProvider {
    /** 从可信登录会话读取用户主体 ID。 */
    @Override
    public long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
