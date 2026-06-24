package cn.jieyuc.generation.domain.model.task;

public enum GenerationTaskType {
    IMAGE,
    VIDEO,
    AUDIO,
    TEXT;

    public static GenerationTaskType of(String value) {
        return value == null ? null : GenerationTaskType.valueOf(value);
    }
}
