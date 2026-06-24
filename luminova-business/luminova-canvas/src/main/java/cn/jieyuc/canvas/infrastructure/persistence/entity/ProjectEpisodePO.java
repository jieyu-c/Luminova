package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project_episode")
public class ProjectEpisodePO extends SoftDeletablePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long projectId;
    private Integer seasonNo;
    private Integer episodeNo;
    private String title;
    private Integer durationMs;
}
