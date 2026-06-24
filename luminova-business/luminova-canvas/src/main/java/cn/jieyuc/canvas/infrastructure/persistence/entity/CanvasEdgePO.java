package cn.jieyuc.canvas.infrastructure.persistence.entity;

import cn.jieyuc.luminova.datasource.typehandler.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "canvas_edge", autoResultMap = true)
public class CanvasEdgePO extends SoftDeletablePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long canvasId;
    private String edgeKey;
    private Long sourceNodeId;
    private Long targetNodeId;
    private String sourceHandle;
    private String targetHandle;
    private String edgeType;
    private String label;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode config;
}
