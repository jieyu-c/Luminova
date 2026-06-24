package cn.jieyuc.canvas.domain.model.canvas;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Builder
public class CanvasRevision implements Serializable {

    private CanvasRevisionId id;
    private CanvasId canvasId;
    private Long revisionNo;
    private Long canvasVersion;
    @Builder.Default
    private Map<String, Object> snapshot = new LinkedHashMap<>();
    private String changeSummary;
    private Long createdBy;
    private OffsetDateTime createdAt;

    public static CanvasRevision capture(
            CanvasId canvasId,
            long revisionNo,
            long canvasVersion,
            Map<String, Object> snapshot,
            Long createdBy,
            String changeSummary
    ) {
        if (canvasId == null) {
            throw new IllegalArgumentException("Canvas id must not be null");
        }
        if (revisionNo <= 0 || canvasVersion < 0) {
            throw new IllegalArgumentException("Revision number and canvas version are invalid");
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("Canvas snapshot must not be null");
        }
        return CanvasRevision.builder()
                .canvasId(canvasId)
                .revisionNo(revisionNo)
                .canvasVersion(canvasVersion)
                .snapshot(new LinkedHashMap<>(snapshot))
                .createdBy(createdBy)
                .changeSummary(changeSummary)
                .build();
    }
}
