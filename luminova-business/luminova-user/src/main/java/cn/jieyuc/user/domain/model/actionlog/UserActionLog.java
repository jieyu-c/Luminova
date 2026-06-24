package cn.jieyuc.user.domain.model.actionlog;

import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.domain.model.user.UserId;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * 用户操作日志，只记录注册、登录等用户相关行为。
 */
@Getter
@Setter
@Builder
public class UserActionLog {
    private UserActionLogId id;
    private UserId userId;
    private UserActionType actionType;
    private Phone phone;
    private String actionIp;
    private String userAgent;
    private Boolean success;
    private String failureReason;
    private OffsetDateTime createdAt;

    public static UserActionLog success(UserId userId, UserActionType actionType, Phone phone) {
        return UserActionLog.builder()
                .userId(userId)
                .actionType(actionType)
                .phone(phone)
                .success(Boolean.TRUE)
                .build();
    }

    public static UserActionLog failure(UserId userId, UserActionType actionType, Phone phone, String failureReason) {
        return UserActionLog.builder()
                .userId(userId)
                .actionType(actionType)
                .phone(phone)
                .success(Boolean.FALSE)
                .failureReason(failureReason)
                .build();
    }
}
