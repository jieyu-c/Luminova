package cn.jieyuc.canvas.domain.model.project;

import java.io.Serializable;

public record ProjectId(Long value) implements Serializable {

    public static ProjectId of(Long value) {
        return value == null ? null : new ProjectId(value);
    }
}
