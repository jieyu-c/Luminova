package cn.jieyuc.file.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** 上传会话持久化实体。 */
@Getter
@Setter
@TableName("file_upload_session")
public class UploadSessionEntity {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long fileId;
    private Long uploaderId;
    private String idempotencyKey;
    private String status;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
    @Version
    private Integer version;
}
