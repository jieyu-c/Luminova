package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.OffsetDateTime;

/** 创作条目持久化实体，仅用于 infrastructure。 */
@TableName("creative_entry")
public class CreativeEntryEntity {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long ownerId;
    private Long parentEntryId;
    private String entryType;
    private String name;
    private Long sortOrder;
    private String customCoverId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    @Version
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getParentEntryId() { return parentEntryId; }
    public void setParentEntryId(Long parentEntryId) { this.parentEntryId = parentEntryId; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getSortOrder() { return sortOrder; }
    public void setSortOrder(Long sortOrder) { this.sortOrder = sortOrder; }
    public String getCustomCoverId() { return customCoverId; }
    public void setCustomCoverId(String customCoverId) { this.customCoverId = customCoverId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
