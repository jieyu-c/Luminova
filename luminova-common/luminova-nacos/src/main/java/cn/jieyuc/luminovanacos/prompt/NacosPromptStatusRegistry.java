package cn.jieyuc.luminovanacos.prompt;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NacosPromptStatusRegistry {

    private final Map<NacosPromptRequest, NacosPromptStatus> statuses = new ConcurrentHashMap<>();

    public void update(NacosPromptRequest request, NacosPromptSource source) {
        update(request, source, null);
    }

    public void update(NacosPromptRequest request, NacosPromptSource source, Throwable error) {
        statuses.put(request, new NacosPromptStatus(
                request,
                source,
                Instant.now(),
                error == null ? "" : error.getMessage()
        ));
    }

    public void updateFallback(NacosPromptRequest request, NacosPromptSource source) {
        NacosPromptStatus existing = statuses.get(request);
        statuses.put(request, new NacosPromptStatus(
                request,
                source,
                Instant.now(),
                existing == null ? "" : existing.lastError()
        ));
    }

    public Optional<NacosPromptStatus> get(NacosPromptRequest request) {
        return Optional.ofNullable(statuses.get(request));
    }

    public Map<NacosPromptRequest, NacosPromptStatus> statuses() {
        return Map.copyOf(statuses);
    }
}
