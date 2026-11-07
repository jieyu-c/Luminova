package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/** 画布类型持久化记录；正文模型后续在此上下文扩展。 */
@TableName("canvas")
public class CanvasEntity {
    @TableId(type = IdType.INPUT)
    private Long entryId;
    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }
}
