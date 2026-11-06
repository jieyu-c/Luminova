package cn.jieyuc.user.infrastructure.id;

import cn.jieyuc.user.application.port.account.UserIdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.stereotype.Component;

/**
 * 基于 MyBatis-Plus 雪花算法的 ID 生成 Adapter。
 */
@Component
public class MybatisPlusUserIdentifierGenerator implements UserIdentifierGenerator {

    @Override
    public long nextId() {
        return IdWorker.getId();
    }
}
