package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class SoftDeletablePO extends AuditablePO {

    @TableLogic(value = "null", delval = "now()")
    private OffsetDateTime deletedAt;
}
