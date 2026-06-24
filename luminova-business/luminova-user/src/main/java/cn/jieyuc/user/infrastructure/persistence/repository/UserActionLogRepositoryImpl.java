package cn.jieyuc.user.infrastructure.persistence.repository;

import cn.jieyuc.user.domain.model.actionlog.UserActionLog;
import cn.jieyuc.user.domain.repository.UserActionLogRepository;
import cn.jieyuc.user.infrastructure.context.UserActionContext;
import cn.jieyuc.user.infrastructure.context.UserActionContextHolder;
import cn.jieyuc.user.infrastructure.persistence.converter.UserPersistenceConverter;
import cn.jieyuc.user.infrastructure.persistence.entity.UserActionLogPO;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserActionLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserActionLogRepositoryImpl implements UserActionLogRepository {
    @Autowired
    private UserActionLogMapper userActionLogMapper;

    private final UserPersistenceConverter converter = UserPersistenceConverter.INSTANCE;

    @Override
    public UserActionLog save(UserActionLog actionLog) {
        if (actionLog == null) {
            throw new IllegalArgumentException("actionLog must not be null");
        }
        UserActionLogPO actionLogPO = converter.toPO(actionLog);
        fillRequestContext(actionLogPO);
        if (actionLogPO.getId() == null) {
            userActionLogMapper.insert(actionLogPO);
        } else {
            userActionLogMapper.updateById(actionLogPO);
        }
        return converter.toDomain(actionLogPO);
    }

    private void fillRequestContext(UserActionLogPO actionLogPO) {
        UserActionContext context = UserActionContextHolder.get();
        if (context == null) {
            return;
        }
        if (actionLogPO.getActionIp() == null || actionLogPO.getActionIp().isBlank()) {
            actionLogPO.setActionIp(context.actionIp());
        }
        if (actionLogPO.getUserAgent() == null || actionLogPO.getUserAgent().isBlank()) {
            actionLogPO.setUserAgent(context.userAgent());
        }
    }
}
