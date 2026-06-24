package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("canvas_member")
public class CanvasMemberPO extends SoftDeletablePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long canvasId;
    private Long userId;
    private String memberRole;
}
