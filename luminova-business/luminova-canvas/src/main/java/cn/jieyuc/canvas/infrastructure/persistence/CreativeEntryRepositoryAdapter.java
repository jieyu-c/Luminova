package cn.jieyuc.canvas.infrastructure.persistence;

import cn.jieyuc.canvas.domain.workspace.CreativeEntry;
import cn.jieyuc.canvas.domain.workspace.CreativeEntryRepository;
import cn.jieyuc.canvas.infrastructure.persistence.conversion.CreativeEntryPersistenceConverter;
import cn.jieyuc.canvas.infrastructure.persistence.entity.CanvasEntity;
import cn.jieyuc.canvas.infrastructure.persistence.entity.CreativeEntryEntity;
import cn.jieyuc.canvas.infrastructure.persistence.entity.CreativeFolderEntity;
import cn.jieyuc.canvas.infrastructure.persistence.entity.RecentlyOpenedCanvasEntity;
import cn.jieyuc.canvas.infrastructure.persistence.mapper.CanvasMapper;
import cn.jieyuc.canvas.infrastructure.persistence.mapper.CreativeEntryMapper;
import cn.jieyuc.canvas.infrastructure.persistence.mapper.CreativeFolderMapper;
import cn.jieyuc.canvas.infrastructure.persistence.mapper.RecentlyOpenedCanvasMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis-Plus 创作条目仓储 Adapter。
 *
 * <p>只负责持久化与查询转换，所有父级合法性和业务状态变化由内层决定。</p>
 */
@Repository
public class CreativeEntryRepositoryAdapter implements CreativeEntryRepository {
    private final CreativeEntryMapper entryMapper;
    private final CreativeFolderMapper folderMapper;
    private final CanvasMapper canvasMapper;
    private final RecentlyOpenedCanvasMapper recentMapper;
    private final CreativeEntryPersistenceConverter converter;

    public CreativeEntryRepositoryAdapter(CreativeEntryMapper entryMapper, CreativeFolderMapper folderMapper,
                                          CanvasMapper canvasMapper,
                                          RecentlyOpenedCanvasMapper recentMapper,
                                          CreativeEntryPersistenceConverter converter) {
        this.entryMapper = entryMapper;
        this.folderMapper = folderMapper;
        this.canvasMapper = canvasMapper;
        this.recentMapper = recentMapper;
        this.converter = converter;
    }

    /** 查询属于指定用户的条目，默认逻辑删除过滤可按需绕开。 */
    @Override
    public Optional<CreativeEntry> find(long ownerId, long entryId, boolean includeDeleted) {
        CreativeEntryEntity entity = includeDeleted
                ? entryMapper.selectById(entryId)
                : entryMapper.selectOne(Wrappers.<CreativeEntryEntity>lambdaQuery()
                        .eq(CreativeEntryEntity::getId, entryId)
                        .eq(CreativeEntryEntity::getOwnerId, ownerId)
                        .isNull(CreativeEntryEntity::getDeletedAt));
        if (entity == null || entity.getOwnerId() != ownerId || (!includeDeleted && entity.getDeletedAt() != null)) {
            return Optional.empty();
        }
        return Optional.of(converter.toDomain(entity));
    }

    /** 使用 MyBatis-Plus 分页查询直属条目。 */
    @Override
    public List<CreativeEntry> listChildren(long ownerId, Long parentId, int offset, int limit) {
        long pageNumber = offset / limit + 1L;
        var query = Wrappers.<CreativeEntryEntity>lambdaQuery()
                .eq(CreativeEntryEntity::getOwnerId, ownerId)
                .isNull(CreativeEntryEntity::getDeletedAt)
                .orderByAsc(CreativeEntryEntity::getSortOrder)
                .orderByAsc(CreativeEntryEntity::getCreatedAt)
                .orderByAsc(CreativeEntryEntity::getId);
        if (parentId == null) {
            query.isNull(CreativeEntryEntity::getParentEntryId);
        } else {
            query.eq(CreativeEntryEntity::getParentEntryId, parentId);
        }
        return entryMapper.selectPage(new Page<>(pageNumber, limit, false), query).getRecords().stream()
                .map(converter::toDomain).toList();
    }

    /** 按名称模糊查询，并过滤处于已删除祖先下的结果。 */
    @Override
    public List<CreativeEntry> search(long ownerId, String keyword, int offset, int limit) {
        return entryMapper.selectList(Wrappers.<CreativeEntryEntity>lambdaQuery()
                        .eq(CreativeEntryEntity::getOwnerId, ownerId)
                        .isNull(CreativeEntryEntity::getDeletedAt)
                        .like(CreativeEntryEntity::getName, keyword)
                        .orderByDesc(CreativeEntryEntity::getUpdatedAt))
                .stream().filter(entity -> hasVisibleAncestors(ownerId, entity.getParentEntryId()))
                .skip(offset).limit(limit).map(converter::toDomain).toList();
    }

    /** 沿父链向上检查候选节点，避免为首期引入闭包表。 */
    @Override
    public boolean isDescendant(long ownerId, long folderId, long candidateId) {
        Long currentId = candidateId;
        int safetyDepth = 0;
        while (currentId != null && safetyDepth++ < 1024) {
            if (currentId == folderId) {
                return true;
            }
            CreativeEntryEntity current = entryMapper.selectById(currentId);
            if (current == null || current.getOwnerId() != ownerId) {
                return false;
            }
            currentId = current.getParentEntryId();
        }
        return false;
    }

    /** 读取目录最大排序值并预留间隔，后续可在不全量改写时插入。 */
    @Override
    public long nextSortOrder(long ownerId, Long parentId) {
        var query = Wrappers.<CreativeEntryEntity>lambdaQuery()
                .eq(CreativeEntryEntity::getOwnerId, ownerId)
                .isNull(CreativeEntryEntity::getDeletedAt)
                .orderByDesc(CreativeEntryEntity::getSortOrder);
        if (parentId == null) {
            query.isNull(CreativeEntryEntity::getParentEntryId);
        } else {
            query.eq(CreativeEntryEntity::getParentEntryId, parentId);
        }
        List<CreativeEntryEntity> records = entryMapper.selectPage(new Page<>(1, 1, false), query).getRecords();
        CreativeEntryEntity last = records.isEmpty() ? null : records.getFirst();
        return last == null ? 1000L : last.getSortOrder() + 1000L;
    }

    /** 插入新行或按 @Version 更新；更新失败代表并发冲突。 */
    @Override
    public CreativeEntry save(CreativeEntry entry) {
        CreativeEntryEntity entity = converter.toEntity(entry);
        int affected = entryMapper.selectById(entity.getId()) == null
                ? entryMapper.insert(entity) : entryMapper.updateById(entity);
        if (affected != 1) {
            throw new IllegalStateException("创作条目已被并发修改，请刷新后重试");
        }
        return converter.toDomain(entryMapper.selectById(entity.getId()));
    }

    /** 建立文件夹类型记录。 */
    @Override
    public void createFolder(long entryId) {
        CreativeFolderEntity entity = new CreativeFolderEntity();
        entity.setEntryId(entryId);
        folderMapper.insert(entity);
    }

    /** 建立画布类型记录。 */
    @Override
    public void createCanvas(long entryId) {
        CanvasEntity entity = new CanvasEntity();
        entity.setEntryId(entryId);
        canvasMapper.insert(entity);
    }

    /** 以用户和画布联合键实现最近打开幂等更新。 */
    @Override
    public void recordOpened(long ownerId, long canvasEntryId, OffsetDateTime openedAt) {
        var condition = Wrappers.<RecentlyOpenedCanvasEntity>lambdaQuery()
                .eq(RecentlyOpenedCanvasEntity::getUserId, ownerId)
                .eq(RecentlyOpenedCanvasEntity::getCanvasEntryId, canvasEntryId);
        RecentlyOpenedCanvasEntity entity = recentMapper.selectOne(condition);
        if (entity == null) {
            entity = new RecentlyOpenedCanvasEntity();
            entity.setUserId(ownerId);
            entity.setCanvasEntryId(canvasEntryId);
            entity.setLastOpenedAt(openedAt);
            recentMapper.insert(entity);
        } else {
            entity.setLastOpenedAt(openedAt);
            recentMapper.update(entity, condition);
        }
    }

    /** 查询最近打开记录后批量映射到仍然可见的画布条目。 */
    @Override
    public List<CreativeEntry> recentlyOpened(long ownerId, int limit) {
        List<RecentlyOpenedCanvasEntity> records = recentMapper.selectPage(new Page<>(1, limit, false),
                Wrappers.<RecentlyOpenedCanvasEntity>lambdaQuery()
                        .eq(RecentlyOpenedCanvasEntity::getUserId, ownerId)
                        .orderByDesc(RecentlyOpenedCanvasEntity::getLastOpenedAt)).getRecords();
        return records.stream().map(record -> find(ownerId, record.getCanvasEntryId(), false).orElse(null))
                .filter(java.util.Objects::nonNull)
                .filter(entry -> hasVisibleAncestors(ownerId, entry.parentId()))
                .sorted(Comparator.comparingInt(entry -> indexOf(records, entry.id())))
                .toList();
    }

    private boolean hasVisibleAncestors(long ownerId, Long parentId) {
        Long currentId = parentId;
        int safetyDepth = 0;
        while (currentId != null && safetyDepth++ < 1024) {
            CreativeEntryEntity parent = entryMapper.selectById(currentId);
            if (parent == null || parent.getOwnerId() != ownerId || parent.getDeletedAt() != null) {
                return false;
            }
            currentId = parent.getParentEntryId();
        }
        return currentId == null;
    }

    private int indexOf(List<RecentlyOpenedCanvasEntity> records, long entryId) {
        for (int index = 0; index < records.size(); index++) {
            if (records.get(index).getCanvasEntryId() == entryId) {
                return index;
            }
        }
        return Integer.MAX_VALUE;
    }
}
