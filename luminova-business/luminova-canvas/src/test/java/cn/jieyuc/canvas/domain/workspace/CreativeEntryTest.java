package cn.jieyuc.canvas.domain.workspace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 创作条目聚合不变量测试。 */
class CreativeEntryTest {
    @Test
    @DisplayName("名称去除首尾空格后持久化")
    void normalizeName() {
        CreativeEntry entry = CreativeEntry.create(1, 2, EntryType.FOLDER, "  素材  ", null, 1000, OffsetDateTime.now());
        assertEquals("素材", entry.name());
    }

    @Test
    @DisplayName("空名称被拒绝")
    void rejectBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> CreativeEntry.create(1, 2, EntryType.CANVAS, "  ", null, 1000, OffsetDateTime.now()));
    }

    @Test
    @DisplayName("条目不能移动到自身")
    void rejectMoveToSelf() {
        CreativeEntry entry = CreativeEntry.create(1, 2, EntryType.FOLDER, "素材", null, 1000, OffsetDateTime.now());
        assertThrows(IllegalArgumentException.class,
                () -> entry.moveTo(1L, 2000, OffsetDateTime.now()));
    }

    @Test
    @DisplayName("删除后不能改名但允许恢复")
    void deletedEntryCannotRename() {
        CreativeEntry entry = CreativeEntry.create(1, 2, EntryType.CANVAS, "画布", null, 1000, OffsetDateTime.now());
        entry.delete(OffsetDateTime.now());
        assertThrows(IllegalStateException.class, () -> entry.rename("新名称", OffsetDateTime.now()));
        entry.restore(null, 2000, OffsetDateTime.now());
        entry.rename("新名称", OffsetDateTime.now());
        assertEquals("新名称", entry.name());
    }
}
