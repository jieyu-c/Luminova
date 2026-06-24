package cn.jieyuc.canvas.infrastructure.persistence.entity;

import cn.jieyuc.luminova.datasource.typehandler.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "canvas_node", autoResultMap = true)
public class CanvasNodePO extends SoftDeletablePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long canvasId;
    private String nodeKey;
    private String nodeType;
    private String title;
    private Short status;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal width;
    private BigDecimal height;
    private Integer zIndex;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode content;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode presentation;
    @Version
    private Long version;
}
