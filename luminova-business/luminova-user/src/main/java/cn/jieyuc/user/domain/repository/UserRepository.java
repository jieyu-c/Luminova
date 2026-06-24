package cn.jieyuc.user.domain.repository;

import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.domain.model.user.Username;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId userId);

    Optional<User> findByPhone(Phone phone);

    Optional<User> findByUsername(Username username);

    User save(User user);
}
