package cn.jieyuc.file.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** 云文件持久化实体，不向业务层暴露。 */
@Getter
@Setter
@TableName("cloud_file")
public class CloudFileEntity {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long uploaderId;
    private String businessContext;
    private String businessObjectType;
    private String businessObjectId;
    private String uploadPurpose;
    private String originalFileName;
    private String mediaType;
    private Long fileSize;
    private String sha256Digest;
    private String storageProvider;
    private String storageBucket;
    private String storageObjectKey;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime availableAt;
    private OffsetDateTime deletedAt;
    private OffsetDateTime purgedAt;
    @Version
    private Integer version;
}
