package cn.jieyuc.user.infrastructure.audit;

import cn.jieyuc.user.domain.model.actionlog.UserActionLog;
import cn.jieyuc.user.domain.repository.UserActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserActionLogRecorder {

    @Autowired
    private UserActionLogRepository userActionLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(UserActionLog actionLog) {
        userActionLogRepository.save(actionLog);
    }
}
