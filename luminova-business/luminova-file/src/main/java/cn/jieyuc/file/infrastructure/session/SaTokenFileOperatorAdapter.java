package cn.jieyuc.file.infrastructure.session;

import cn.dev33.satoken.stp.StpUtil;
import cn.jieyuc.file.application.port.AuthenticatedFileOperator;
import org.springframework.stereotype.Component;

/** 从 Sa-Token 可信会话读取当前文件操作者，不接受请求体伪造用户标识。 */
@Component
public class SaTokenFileOperatorAdapter implements AuthenticatedFileOperator {
    /** 返回当前登录用户标识，未登录时由 Sa-Token 拒绝。 */
    @Override
    public long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
