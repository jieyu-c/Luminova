package cn.jieyuc.user.domain.repository;

import cn.jieyuc.user.domain.model.actionlog.UserActionLog;

public interface UserActionLogRepository {
    UserActionLog save(UserActionLog actionLog);
}
