package cn.jieyuc.canvas.application.port.session;

/** 当前会话创作者提供端口。 */
public interface AuthenticatedCreatorProvider {
    /** 返回可信会话中的用户主体 ID。 */
    long currentUserId();
}
