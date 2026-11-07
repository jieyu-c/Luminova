package cn.jieyuc.canvas.application.workspace;

import cn.jieyuc.canvas.domain.workspace.EntryType;
import java.time.OffsetDateTime;

/** 工作台创作条目应用视图。 */
public record CreativeEntryView(long id, EntryType type, String name, Long parentId,
                                long sortOrder, String customCoverId, OffsetDateTime createdAt,
                                OffsetDateTime updatedAt, long version) {
}
