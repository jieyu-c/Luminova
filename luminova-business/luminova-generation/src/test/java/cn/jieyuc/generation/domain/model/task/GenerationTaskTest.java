package cn.jieyuc.generation.domain.model.task;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerationTaskTest {

    @Test
    void shouldCompleteGenerationTask() {
        GenerationTask task = GenerationTask.create(
                1L,
                2L,
                3L,
                GenerationTaskType.VIDEO,
                Map.of("prompt", "rainy convenience store")
        );

        task.start();
        task.succeed(Map.of("videoUrl", "https://cdn.example.com/video.mp4"));

        assertEquals(GenerationTaskStatus.SUCCEEDED, task.getStatus());
        assertEquals(
                "https://cdn.example.com/video.mp4",
                task.getResultData().get("videoUrl")
        );
    }
}
