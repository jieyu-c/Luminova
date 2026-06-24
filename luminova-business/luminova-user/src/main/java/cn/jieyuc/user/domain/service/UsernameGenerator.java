package cn.jieyuc.user.domain.service;

import cn.jieyuc.user.domain.model.user.Username;

public interface UsernameGenerator {

    Username generateTemporary();
}
