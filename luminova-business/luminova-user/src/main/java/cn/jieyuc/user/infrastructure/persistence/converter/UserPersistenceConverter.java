package cn.jieyuc.user.infrastructure.persistence.converter;

import cn.jieyuc.user.domain.model.user.Email;
import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.actionlog.UserActionLog;
import cn.jieyuc.user.domain.model.actionlog.UserActionLogId;
import cn.jieyuc.user.domain.model.actionlog.UserActionType;
import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.domain.model.user.UserStatus;
import cn.jieyuc.user.infrastructure.persistence.entity.UserActionLogPO;
import cn.jieyuc.user.infrastructure.persistence.entity.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserPersistenceConverter {

    UserPersistenceConverter INSTANCE = Mappers.getMapper(UserPersistenceConverter.class);

    User toDomain(UserPO po);

    @Mapping(target = "passwordUpdatedAt", ignore = true)
    @Mapping(target = "phoneVerifiedAt", ignore = true)
    @Mapping(target = "emailVerifiedAt", ignore = true)
    @Mapping(target = "failedLoginCount", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    UserPO toPO(User user);

    UserActionLog toDomain(UserActionLogPO po);

    UserActionLogPO toPO(UserActionLog actionLog);

    default UserId map(Long value) {
        return value == null ? null : new UserId(value);
    }

    default Long map(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default UserActionLogId mapActionLogId(Long value) {
        return value == null ? null : new UserActionLogId(value);
    }

    default Long map(UserActionLogId actionLogId) {
        return actionLogId == null ? null : actionLogId.value();
    }

    default Phone map(String value) {
        return value == null ? null : Phone.of(value);
    }

    default String map(Phone phone) {
        return phone == null ? null : phone.value();
    }

    default Email mapEmail(String value) {
        return value == null ? null : new Email(value);
    }

    default String map(Email email) {
        return email == null ? null : email.value();
    }

    default UserStatus map(Short value) {
        return UserStatus.of(value);
    }

    default Short map(UserStatus status) {
        return status == null ? null : status.code();
    }

    default UserActionType mapActionType(String value) {
        return UserActionType.of(value);
    }

    default String map(UserActionType actionType) {
        return actionType == null ? null : actionType.name();
    }
}
