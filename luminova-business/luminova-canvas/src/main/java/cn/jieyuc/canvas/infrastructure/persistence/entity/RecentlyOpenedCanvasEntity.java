package cn.jieyuc.canvas.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;

/** 最近打开画布持久化实体。 */
@TableName("recently_opened_canvas")
public class RecentlyOpenedCanvasEntity {
    private Long userId;
    private Long canvasEntryId;
    private OffsetDateTime lastOpenedAt;
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCanvasEntryId() { return canvasEntryId; }
    public void setCanvasEntryId(Long canvasEntryId) { this.canvasEntryId = canvasEntryId; }
    public OffsetDateTime getLastOpenedAt() { return lastOpenedAt; }
    public void setLastOpenedAt(OffsetDateTime lastOpenedAt) { this.lastOpenedAt = lastOpenedAt; }
}
