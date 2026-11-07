package cn.jieyuc.canvas.domain.workspace;

import java.time.OffsetDateTime;

/**
 * 创作条目聚合根，维护名称、目录归属、统一排序、软删除和版本不变量。
 * 画布正文不属于本聚合。
 */
public final class CreativeEntry {
    public static final int MAX_NAME_LENGTH = 100;
    private final long id;
    private final long ownerId;
    private final EntryType type;
    private String name;
    private Long parentId;
    private long sortOrder;
    private String customCoverId;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private final long version;

    public CreativeEntry(long id, long ownerId, EntryType type, String name, Long parentId,
                         long sortOrder, String customCoverId, OffsetDateTime createdAt,
                         OffsetDateTime updatedAt, OffsetDateTime deletedAt, long version) {
        if (id <= 0 || ownerId <= 0 || type == null || createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("创作条目基础字段无效");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.type = type;
        this.name = normalizeName(name);
        this.parentId = parentId;
        this.sortOrder = sortOrder;
        this.customCoverId = customCoverId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    /** 创建一个处于正常状态的新条目。 */
    public static CreativeEntry create(long id, long ownerId, EntryType type, String name,
                                       Long parentId, long sortOrder, OffsetDateTime now) {
        return new CreativeEntry(id, ownerId, type, name, parentId, sortOrder, null, now, now, null, 0);
    }

    /** 重命名条目。 */
    public void rename(String newName, OffsetDateTime now) {
        requireActive();
        name = normalizeName(newName);
        updatedAt = now;
    }

    /** 修改自定义封面引用。 */
    public void changeCustomCover(String coverId, OffsetDateTime now) {
        requireActive();
        customCoverId = coverId == null || coverId.isBlank() ? null : coverId.trim();
        updatedAt = now;
    }

    /** 移动条目；环检测由应用层完成。 */
    public void moveTo(Long targetParentId, long targetSortOrder, OffsetDateTime now) {
        requireActive();
        if (targetParentId != null && targetParentId == id) {
            throw new IllegalArgumentException("文件夹不能移动到自身");
        }
        parentId = targetParentId;
        sortOrder = targetSortOrder;
        updatedAt = now;
    }

    /** 调整同一目录中的手动顺序。 */
    public void reorder(long targetSortOrder, OffsetDateTime now) {
        requireActive();
        sortOrder = targetSortOrder;
        updatedAt = now;
    }

    /** 软删除条目，重复删除保持幂等。 */
    public void delete(OffsetDateTime now) {
        if (deletedAt == null) {
            deletedAt = now;
            updatedAt = now;
        }
    }

    /** 恢复到应用层验证后的有效父级。 */
    public void restore(Long validParentId, long targetSortOrder, OffsetDateTime now) {
        deletedAt = null;
        parentId = validParentId;
        sortOrder = targetSortOrder;
        updatedAt = now;
    }

    private static String normalizeName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("条目名称不能为空");
        }
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.length() > MAX_NAME_LENGTH || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("条目名称必须为 1 到 100 个非控制字符");
        }
        return normalized;
    }

    private void requireActive() {
        if (deletedAt != null) {
            throw new IllegalStateException("已删除条目不能执行该操作");
        }
    }

    public long id() {
        return id;
    }
    public long ownerId() {
        return ownerId;
    }
    public EntryType type() {
        return type;
    }
    public String name() {
        return name;
    }
    public Long parentId() {
        return parentId;
    }
    public long sortOrder() {
        return sortOrder;
    }
    public String customCoverId() {
        return customCoverId;
    }
    public OffsetDateTime createdAt() {
        return createdAt;
    }
    public OffsetDateTime updatedAt() {
        return updatedAt;
    }
    public OffsetDateTime deletedAt() {
        return deletedAt;
    }
    public long version() {
        return version;
    }
}
