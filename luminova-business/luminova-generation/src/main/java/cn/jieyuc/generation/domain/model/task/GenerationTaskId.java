package cn.jieyuc.generation.domain.model.task;

import java.io.Serializable;

public record GenerationTaskId(Long value) implements Serializable {

    public static GenerationTaskId of(Long value) {
        return value == null ? null : new GenerationTaskId(value);
    }
}
