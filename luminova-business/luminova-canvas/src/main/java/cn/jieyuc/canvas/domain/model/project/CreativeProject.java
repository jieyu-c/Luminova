package cn.jieyuc.canvas.domain.model.project;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class CreativeProject implements Serializable {

    private ProjectId id;
    private Long ownerId;
    private String name;
    private ProjectType projectType;
    private String description;
    private String coverUrl;
    private String aspectRatio;
    private Integer targetDurationMs;
    private ProjectStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static CreativeProject create(Long ownerId, String name, ProjectType projectType) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Project owner must not be null");
        }
        return CreativeProject.builder()
                .ownerId(ownerId)
                .name(requireName(name))
                .projectType(requireType(projectType))
                .status(ProjectStatus.DRAFT)
                .build();
    }

    public void rename(String name) {
        this.name = requireName(name);
    }

    public void start() {
        this.status = ProjectStatus.IN_PROGRESS;
    }

    public void submitForReview() {
        if (status != ProjectStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only an in-progress project can be submitted for review");
        }
        this.status = ProjectStatus.PENDING_REVIEW;
    }

    public void complete() {
        this.status = ProjectStatus.COMPLETED;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank");
        }
        return name.trim();
    }

    private static ProjectType requireType(ProjectType projectType) {
        if (projectType == null) {
            throw new IllegalArgumentException("Project type must not be null");
        }
        return projectType;
    }
}
