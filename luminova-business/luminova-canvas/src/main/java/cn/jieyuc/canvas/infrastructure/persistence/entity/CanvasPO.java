package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("canvas")
public class CanvasPO extends SoftDeletablePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long projectId;
    private Long episodeId;
    private Long ownerId;
    private String name;
    private String canvasType;
    private Short status;
    private String coverUrl;
    private BigDecimal viewportX;
    private BigDecimal viewportY;
    private BigDecimal viewportZoom;
    @Version
    private Long version;
    private Long lastEditedBy;
    private OffsetDateTime lastEditedAt;
}
