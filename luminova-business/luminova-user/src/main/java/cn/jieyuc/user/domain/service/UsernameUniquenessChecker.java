package cn.jieyuc.user.domain.service;

import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.domain.model.user.Username;

public interface UsernameUniquenessChecker {

    boolean isAvailable(Username username);

    boolean isAvailable(Username username, UserId excludeUserId);

    void markUsed(Username username);
}
