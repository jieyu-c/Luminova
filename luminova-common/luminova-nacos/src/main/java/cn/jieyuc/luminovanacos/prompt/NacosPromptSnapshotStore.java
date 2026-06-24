package cn.jieyuc.luminovanacos.prompt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NacosPromptSnapshotStore {

    private static final Logger log = LoggerFactory.getLogger(NacosPromptSnapshotStore.class);

    private final ObjectMapper objectMapper;
    private final Path snapshotPath;

    public NacosPromptSnapshotStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.snapshotPath = Path.of(System.getProperty("user.dir"), "target", "nacos-prompt-snapshot.json");
    }

    public Map<NacosPromptRequest, NacosPromptValue> load() {
        if (!Files.isRegularFile(snapshotPath)) {
            return Map.of();
        }

        try {
            Map<String, SnapshotEntry> entries = objectMapper.readValue(
                    Files.readString(snapshotPath),
                    new TypeReference<>() {
                    }
            );
            Map<NacosPromptRequest, NacosPromptValue> snapshots = new HashMap<>();
            entries.forEach((ignored, entry) -> {
                if (entry != null && entry.key() != null) {
                    snapshots.put(
                            new NacosPromptRequest(entry.key(), entry.version(), entry.label()),
                            new NacosPromptValue(entry.template(), entry.md5(), entry.promptVersion())
                    );
                }
            });
            return snapshots;
        } catch (IOException exception) {
            log.warn("Failed to load Nacos prompt snapshot from {}", snapshotPath, exception);
            return Map.of();
        }
    }

    public Optional<NacosPromptValue> get(NacosPromptRequest request) {
        return Optional.ofNullable(load().get(request));
    }

    public void save(Map<NacosPromptRequest, NacosPromptValue> prompts) {
        Map<String, SnapshotEntry> entries = new HashMap<>();
        prompts.forEach((request, value) -> entries.put(request.snapshotKey(), SnapshotEntry.from(request, value)));

        try {
            Files.createDirectories(snapshotPath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(snapshotPath.toFile(), entries);
        } catch (IOException exception) {
            log.warn("Failed to save Nacos prompt snapshot to {}", snapshotPath, exception);
        }
    }

    public void save(NacosPromptRequest request, NacosPromptValue value) {
        Map<NacosPromptRequest, NacosPromptValue> prompts = new HashMap<>(load());
        prompts.put(request, value);
        save(prompts);
    }

    private record SnapshotEntry(
            String key,
            String version,
            String label,
            String template,
            String md5,
            String promptVersion
    ) {

        private static SnapshotEntry from(NacosPromptRequest request, NacosPromptValue value) {
            return new SnapshotEntry(
                    request.key(),
                    request.version(),
                    request.label(),
                    value.template(),
                    value.md5(),
                    value.version()
            );
        }
    }
}
