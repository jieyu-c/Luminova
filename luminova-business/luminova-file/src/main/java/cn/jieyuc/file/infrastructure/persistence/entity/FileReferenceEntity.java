package cn.jieyuc.file.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** 文件引用账本持久化实体。 */
@Getter
@Setter
@TableName("file_reference")
public class FileReferenceEntity {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long fileId;
    private String businessContext;
    private String businessObjectType;
    private String businessObjectId;
    private String referenceType;
    private String referenceKey;
    private OffsetDateTime createdAt;
    private OffsetDateTime releasedAt;
}
