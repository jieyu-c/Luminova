package cn.jieyuc.canvas.interfaces.dto;

import jakarta.validation.constraints.PositiveOrZero;

/** 手动调整创作条目顺序请求。 */
public record ReorderCreativeEntryRequest(@PositiveOrZero long sortOrder) {
}
