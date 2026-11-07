package cn.jieyuc.canvas.application.workspace;

import cn.jieyuc.canvas.application.port.session.AuthenticatedCreatorProvider;
import cn.jieyuc.canvas.application.port.session.CreativeEntryIdentifierGenerator;
import cn.jieyuc.canvas.domain.workspace.CreativeEntry;
import cn.jieyuc.canvas.domain.workspace.CreativeEntryRepository;
import cn.jieyuc.canvas.domain.workspace.EntryType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 工作台目录用例服务。
 *
 * <p>负责所有权、父级类型、文件夹环和事务协调，不处理 HTTP 协议或画布正文。</p>
 */
@Service
public class WorkspaceEntryApplicationService {
    private final CreativeEntryRepository repository;
    private final AuthenticatedCreatorProvider creatorProvider;
    private final CreativeEntryIdentifierGenerator identifierGenerator;
    private final CreativeEntryViewConverter viewConverter;

    public WorkspaceEntryApplicationService(CreativeEntryRepository repository,
                                            AuthenticatedCreatorProvider creatorProvider,
                                            CreativeEntryIdentifierGenerator identifierGenerator,
                                            CreativeEntryViewConverter viewConverter) {
        this.repository = repository;
        this.creatorProvider = creatorProvider;
        this.identifierGenerator = identifierGenerator;
        this.viewConverter = viewConverter;
    }

    /** 查询当前目录直属条目。 */
    @Transactional(readOnly = true)
    public List<CreativeEntryView> list(Long parentId, int page, int size) {
        long ownerId = creatorProvider.currentUserId();
        validateParent(ownerId, parentId);
        return repository.listChildren(ownerId, parentId, page * size, size).stream().map(viewConverter::toView).toList();
    }

    /** 搜索当前用户全部活跃条目。 */
    @Transactional(readOnly = true)
    public List<CreativeEntryView> search(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        long ownerId = creatorProvider.currentUserId();
        return repository.search(ownerId, keyword.trim(), page * size, size).stream().map(viewConverter::toView).toList();
    }

    /** 在目标目录创建文件夹。 */
    @Transactional
    public CreativeEntryView createFolder(Long parentId, String name) {
        CreativeEntryView view = create(parentId, name, EntryType.FOLDER);
        repository.createFolder(view.id());
        return view;
    }

    /** 在目标目录创建画布，同时建立稳定的一对一画布记录。 */
    @Transactional
    public CreativeEntryView createCanvas(Long parentId, String name) {
        CreativeEntryView view = create(parentId, name, EntryType.CANVAS);
        repository.createCanvas(view.id());
        return view;
    }

    /** 修改通用条目属性，未提供的字段保持不变。 */
    @Transactional
    public CreativeEntryView update(long entryId, String name, String customCoverId, boolean coverProvided) {
        CreativeEntry entry = loadActive(entryId);
        OffsetDateTime now = OffsetDateTime.now();
        if (name != null) {
            entry.rename(name, now);
        }
        if (coverProvided) {
            entry.changeCustomCover(customCoverId, now);
        }
        return viewConverter.toView(repository.save(entry));
    }

    /** 移动条目，并拒绝画布父级、跨用户父级和文件夹循环。 */
    @Transactional
    public CreativeEntryView move(long entryId, Long targetParentId) {
        long ownerId = creatorProvider.currentUserId();
        CreativeEntry entry = loadActive(entryId);
        validateParent(ownerId, targetParentId);
        if (entry.type() == EntryType.FOLDER && targetParentId != null &&
                repository.isDescendant(ownerId, entry.id(), targetParentId)) {
            throw new IllegalArgumentException("文件夹不能移动到自身后代");
        }
        entry.moveTo(targetParentId, repository.nextSortOrder(ownerId, targetParentId), OffsetDateTime.now());
        return viewConverter.toView(repository.save(entry));
    }

    /** 调整条目在当前目录中的手动顺序。 */
    @Transactional
    public CreativeEntryView reorder(long entryId, long sortOrder) {
        CreativeEntry entry = loadActive(entryId);
        entry.reorder(sortOrder, OffsetDateTime.now());
        return viewConverter.toView(repository.save(entry));
    }

    /** 软删除单个条目；文件夹后代通过祖先可见性规则隐藏。 */
    @Transactional
    public void delete(long entryId) {
        CreativeEntry entry = loadActive(entryId);
        entry.delete(OffsetDateTime.now());
        repository.save(entry);
    }

    /** 恢复条目；原父级失效时恢复到根目录。 */
    @Transactional
    public CreativeEntryView restore(long entryId) {
        long ownerId = creatorProvider.currentUserId();
        CreativeEntry entry = repository.find(ownerId, entryId, true)
                .orElseThrow(() -> new IllegalArgumentException("创作条目不存在"));
        Long parentId = validRestoreParent(ownerId, entry.parentId());
        entry.restore(parentId, repository.nextSortOrder(ownerId, parentId), OffsetDateTime.now());
        return viewConverter.toView(repository.save(entry));
    }

    /** 记录当前用户打开画布；重复调用仅更新时间。 */
    @Transactional
    public void recordOpened(long canvasEntryId) {
        CreativeEntry entry = loadActive(canvasEntryId);
        if (entry.type() != EntryType.CANVAS) {
            throw new IllegalArgumentException("只有画布可以记录最近打开");
        }
        repository.recordOpened(entry.ownerId(), entry.id(), OffsetDateTime.now());
    }

    /** 查询当前用户最近打开画布。 */
    @Transactional(readOnly = true)
    public List<CreativeEntryView> recentlyOpened(int limit) {
        return repository.recentlyOpened(creatorProvider.currentUserId(), limit).stream().map(viewConverter::toView).toList();
    }

    private CreativeEntryView create(Long parentId, String name, EntryType type) {
        long ownerId = creatorProvider.currentUserId();
        validateParent(ownerId, parentId);
        CreativeEntry entry = CreativeEntry.create(identifierGenerator.nextId(), ownerId, type, name,
                parentId, repository.nextSortOrder(ownerId, parentId), OffsetDateTime.now());
        return viewConverter.toView(repository.save(entry));
    }

    private CreativeEntry loadActive(long entryId) {
        return repository.find(creatorProvider.currentUserId(), entryId, false)
                .orElseThrow(() -> new IllegalArgumentException("创作条目不存在"));
    }

    private void validateParent(long ownerId, Long parentId) {
        if (parentId == null) {
            return;
        }
        CreativeEntry parent = repository.find(ownerId, parentId, false)
                .orElseThrow(() -> new IllegalArgumentException("目标文件夹不存在"));
        if (parent.type() != EntryType.FOLDER) {
            throw new IllegalArgumentException("目标父级必须是文件夹");
        }
    }

    private Long validRestoreParent(long ownerId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        return repository.find(ownerId, parentId, false).filter(entry -> entry.type() == EntryType.FOLDER)
                .map(CreativeEntry::id).orElse(null);
    }
}
