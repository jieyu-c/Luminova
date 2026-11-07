package cn.jieyuc.canvas.infrastructure.id;

import cn.jieyuc.canvas.application.port.session.CreativeEntryIdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.stereotype.Component;

/** 基于 MyBatis-Plus 雪花算法的创作条目 ID Adapter。 */
@Component
public class MybatisPlusCreativeEntryIdentifierGenerator implements CreativeEntryIdentifierGenerator {
    /** 生成全局唯一正数 ID。 */
    @Override
    public long nextId() {
        return IdWorker.getId();
    }
}
