package cn.jieyuc.canvas.interfaces.dto;

import jakarta.validation.constraints.Size;

/** 更新创作条目通用属性请求。 */
public record UpdateCreativeEntryRequest(@Size(max = 100) String name,
                                         String customCoverId,
                                         Boolean clearCustomCover) {
}
