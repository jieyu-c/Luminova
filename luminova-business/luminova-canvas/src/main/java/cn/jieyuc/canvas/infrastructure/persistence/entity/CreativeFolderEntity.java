package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/** creative_folder 类型表实体。 */
@TableName("creative_folder")
public class CreativeFolderEntity {
    @TableId
    private Long entryId;

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }
}
