package cn.jieyuc.canvas.infrastructure.persistence.entity;

import cn.jieyuc.luminova.datasource.typehandler.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(value = "canvas_revision", autoResultMap = true)
public class CanvasRevisionPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long canvasId;
    private Long revisionNo;
    private Long canvasVersion;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode snapshot;
    private String changeSummary;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
