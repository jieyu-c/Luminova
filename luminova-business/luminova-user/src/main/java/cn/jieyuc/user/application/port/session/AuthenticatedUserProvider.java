package cn.jieyuc.user.application.port.session;

/**
 * 当前认证用户端口。
 *
 * <p>应用层只依赖稳定用户 ID，不感知 Sa-Token 或 HTTP 请求。</p>
 */
public interface AuthenticatedUserProvider {

    /** 返回当前会话的用户主体 ID；未登录时由认证框架抛出统一异常。 */
    long currentUserId();
}
