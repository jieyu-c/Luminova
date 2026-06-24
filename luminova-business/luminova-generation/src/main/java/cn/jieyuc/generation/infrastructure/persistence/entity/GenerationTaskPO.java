package cn.jieyuc.generation.infrastructure.persistence.entity;

import cn.jieyuc.luminova.datasource.typehandler.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(value = "generation_task", autoResultMap = true)
public class GenerationTaskPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long canvasId;
    private Long nodeId;
    private Long userId;
    private String taskType;
    private String provider;
    private String modelName;
    private Short status;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode requestParams;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode resultData;
    private String errorCode;
    private String errorMessage;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private OffsetDateTime createdAt;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
