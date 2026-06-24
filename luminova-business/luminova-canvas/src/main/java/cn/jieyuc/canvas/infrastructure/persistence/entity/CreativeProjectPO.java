package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("creative_project")
public class CreativeProjectPO extends SoftDeletablePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long ownerId;
    private String name;
    private String projectType;
    private String description;
    private String coverUrl;
    private String aspectRatio;
    private Integer targetDurationMs;
    private Short status;
}
