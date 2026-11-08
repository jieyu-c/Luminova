package cn.jieyuc.file.infrastructure.id;

import cn.jieyuc.file.application.port.FileIdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.stereotype.Component;

/** 使用 MyBatis-Plus 雪花算法生成文件上下文标识。 */
@Component
public class MybatisPlusFileIdentifierGenerator implements FileIdentifierGenerator {
    /** 返回下一个正数雪花标识。 */
    @Override
    public long nextId() {
        return IdWorker.getId();
    }
}
