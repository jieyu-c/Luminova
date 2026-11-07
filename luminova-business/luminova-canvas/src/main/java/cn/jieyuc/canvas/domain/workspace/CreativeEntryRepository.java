package cn.jieyuc.canvas.domain.workspace;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** 创作条目仓储端口；所有查询显式携带 ownerId 以阻止跨用户访问。 */
public interface CreativeEntryRepository {
    /** 按所有者和 ID 查询，可选择包含软删除记录。 */
    Optional<CreativeEntry> find(long ownerId, long entryId, boolean includeDeleted);
    /** 分页查询一个目录的直属活跃条目。 */
    List<CreativeEntry> listChildren(long ownerId, Long parentId, int offset, int limit);
    /** 按名称搜索当前用户的活跃条目。 */
    List<CreativeEntry> search(long ownerId, String keyword, int offset, int limit);
    /** 判断候选节点是否位于文件夹后代中。 */
    boolean isDescendant(long ownerId, long folderId, long candidateId);
    /** 返回目录末尾的下一个排序值。 */
    long nextSortOrder(long ownerId, Long parentId);
    /** 新增或按乐观锁更新聚合。 */
    CreativeEntry save(CreativeEntry entry);
    /** 建立文件夹类型记录。 */
    void createFolder(long entryId);
    /** 建立画布类型记录。 */
    void createCanvas(long entryId);
    /** 记录画布最近打开时间。 */
    void recordOpened(long ownerId, long canvasEntryId, OffsetDateTime openedAt);
    /** 查询最近打开的画布。 */
    List<CreativeEntry> recentlyOpened(long ownerId, int limit);
}
