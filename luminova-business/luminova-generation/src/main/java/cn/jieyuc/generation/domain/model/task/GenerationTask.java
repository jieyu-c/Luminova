package cn.jieyuc.generation.domain.model.task;
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
public class GenerationTask implements Serializable {

    private GenerationTaskId id;
    private Long canvasId;
    private Long nodeId;
    private Long userId;
    private GenerationTaskType taskType;
    private String provider;
    private String modelName;
    private GenerationTaskStatus status;
    @Builder.Default
    private Map<String, Object> requestParams = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> resultData = new LinkedHashMap<>();
    private String errorCode;
    private String errorMessage;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static GenerationTask create(
            Long canvasId,
            Long nodeId,
            Long userId,
            GenerationTaskType taskType,
            Map<String, Object> requestParams
    ) {
        if (canvasId == null || userId == null || taskType == null) {
            throw new IllegalArgumentException("Canvas, user and task type must not be null");
        }
        return GenerationTask.builder()
                .canvasId(canvasId)
                .nodeId(nodeId)
                .userId(userId)
                .taskType(taskType)
                .status(GenerationTaskStatus.PENDING)
                .requestParams(requestParams == null
                        ? new LinkedHashMap<>()
                        : new LinkedHashMap<>(requestParams))
                .build();
    }

    public void start() {
        if (status != GenerationTaskStatus.PENDING) {
            throw new IllegalStateException("Only a pending generation task can start");
        }
        this.status = GenerationTaskStatus.RUNNING;
        this.startedAt = OffsetDateTime.now();
    }

    public void succeed(Map<String, Object> resultData) {
        ensureRunning();
        this.status = GenerationTaskStatus.SUCCEEDED;
        this.resultData = resultData == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(resultData);
        this.finishedAt = OffsetDateTime.now();
    }

    public void fail(String errorCode, String errorMessage) {
        ensureRunning();
        this.status = GenerationTaskStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.finishedAt = OffsetDateTime.now();
    }

    public void cancel() {
        if (status != GenerationTaskStatus.PENDING && status != GenerationTaskStatus.RUNNING) {
            throw new IllegalStateException("Only a pending or running generation task can be cancelled");
        }
        this.status = GenerationTaskStatus.CANCELLED;
        this.finishedAt = OffsetDateTime.now();
    }

    private void ensureRunning() {
        if (status != GenerationTaskStatus.RUNNING) {
            throw new IllegalStateException("Generation task is not running");
        }
    }
}
