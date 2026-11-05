package cn.jieyuc.luminovaauth.application.session;

import cn.jieyuc.luminovaauth.application.login.AuthenticatedPrincipal;

/**
 * 登录会话端口。
 *
 * <p>接口隐藏 Sa-Token、Redis key 和 Token 生成细节，调用方只使用稳定用户主体 ID。</p>
 */
public interface SessionIssuer {

    /** 为认证主体签发会话。 */
    IssuedSession issue(AuthenticatedPrincipal principal);

    /** 幂等注销当前请求携带的会话。 */
    void revokeCurrent();

    /** 撤销指定用户主体的全部会话。 */
    void revokeAll(long userId);
}
