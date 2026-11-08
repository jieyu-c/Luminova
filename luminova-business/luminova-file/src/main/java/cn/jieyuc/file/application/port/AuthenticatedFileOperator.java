package cn.jieyuc.file.application.port;

/** 提供经过认证的当前文件操作者。 */
public interface AuthenticatedFileOperator {
    /** 返回当前登录用户的稳定标识。 */
    long currentUserId();
}
