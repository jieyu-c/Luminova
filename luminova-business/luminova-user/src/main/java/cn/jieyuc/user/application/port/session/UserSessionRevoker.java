package cn.jieyuc.user.application.port.session;

/**
 * 用户会话撤销端口。
 */
public interface UserSessionRevoker {

    /** 撤销指定用户主体的全部会话。 */
    void revokeAll(long userId);
}
