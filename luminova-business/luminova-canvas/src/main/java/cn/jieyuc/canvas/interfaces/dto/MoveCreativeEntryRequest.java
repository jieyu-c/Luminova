package cn.jieyuc.canvas.interfaces.dto;

/** 移动创作条目请求，targetParentId 为空表示根目录。 */
public record MoveCreativeEntryRequest(Long targetParentId) {
}
