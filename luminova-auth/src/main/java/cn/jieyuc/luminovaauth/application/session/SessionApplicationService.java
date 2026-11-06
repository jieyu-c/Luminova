package cn.jieyuc.luminovaauth.application.session;

import cn.jieyuc.luminovaauth.application.port.session.SessionIssuer;

import org.springframework.stereotype.Service;

/**
 * 会话生命周期应用服务。
 *
 * <p>为协议层提供稳定用例入口，不暴露 Sa-Token 静态方法。</p>
 */
@Service
public class SessionApplicationService {
    private final SessionIssuer sessionIssuer;

    public SessionApplicationService(SessionIssuer sessionIssuer) {
        this.sessionIssuer = sessionIssuer;
    }

    /** 幂等注销当前会话。 */
    public void logout() {
        sessionIssuer.revokeCurrent();
    }
}
