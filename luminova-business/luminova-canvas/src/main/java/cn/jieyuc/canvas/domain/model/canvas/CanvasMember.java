package cn.jieyuc.canvas.domain.model.canvas;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class CanvasMember implements Serializable {

    private CanvasMemberId id;
    private CanvasId canvasId;
    private Long userId;
    private CanvasMemberRole memberRole;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static CanvasMember join(CanvasId canvasId, Long userId, CanvasMemberRole role) {
        if (canvasId == null || userId == null) {
            throw new IllegalArgumentException("Canvas id and user id must not be null");
        }
        return CanvasMember.builder()
                .canvasId(canvasId)
                .userId(userId)
                .memberRole(role == null ? CanvasMemberRole.VIEWER : role)
                .build();
    }

    public void changeRole(CanvasMemberRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Canvas member role must not be null");
        }
        this.memberRole = role;
    }

    public boolean canEdit() {
        return memberRole == CanvasMemberRole.OWNER || memberRole == CanvasMemberRole.EDITOR;
    }
}
