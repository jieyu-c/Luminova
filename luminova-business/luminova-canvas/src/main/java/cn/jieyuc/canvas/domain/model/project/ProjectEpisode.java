package cn.jieyuc.canvas.domain.model.project;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class ProjectEpisode implements Serializable {

    private EpisodeId id;
    private ProjectId projectId;
    private Integer seasonNo;
    private Integer episodeNo;
    private String title;
    private Integer durationMs;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static ProjectEpisode create(ProjectId projectId, int seasonNo, int episodeNo, String title) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project id must not be null");
        }
        if (seasonNo <= 0 || episodeNo <= 0) {
            throw new IllegalArgumentException("Season and episode numbers must be positive");
        }
        return ProjectEpisode.builder()
                .projectId(projectId)
                .seasonNo(seasonNo)
                .episodeNo(episodeNo)
                .title(title == null ? null : title.trim())
                .build();
    }

    public void changeDuration(Integer durationMs) {
        if (durationMs != null && durationMs < 0) {
            throw new IllegalArgumentException("Episode duration must not be negative");
        }
        this.durationMs = durationMs;
    }
}
