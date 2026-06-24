package cn.jieyuc.canvas.domain.model.project;

import java.io.Serializable;

public record EpisodeId(Long value) implements Serializable {

    public static EpisodeId of(Long value) {
        return value == null ? null : new EpisodeId(value);
    }
}
