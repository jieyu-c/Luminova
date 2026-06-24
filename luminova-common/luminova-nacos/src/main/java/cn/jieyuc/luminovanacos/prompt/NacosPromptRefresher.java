package cn.jieyuc.luminovanacos.prompt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NacosPromptRefresher implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(NacosPromptRefresher.class);
    private static final long REFRESH_INTERVAL_MILLIS = 30_000;

    private final NacosPromptClient client;
    private final NacosPromptCache cache;
    private final NacosPromptRegistry registry;
    private final NacosPromptSnapshotStore snapshotStore;
    private final NacosPromptStatusRegistry statusRegistry;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;

    public NacosPromptRefresher(
            NacosPromptClient client,
            NacosPromptCache cache,
            NacosPromptRegistry registry,
            NacosPromptSnapshotStore snapshotStore,
            NacosPromptStatusRegistry statusRegistry
    ) {
        this.client = client;
        this.cache = cache;
        this.registry = registry;
        this.snapshotStore = snapshotStore;
        this.statusRegistry = statusRegistry;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "nacos-prompt-refresher");
            thread.setDaemon(true);
            return thread;
        });

        executorService.scheduleWithFixedDelay(
                this::refreshSafely,
                REFRESH_INTERVAL_MILLIS,
                REFRESH_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void stop() {
        running.set(false);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void refreshSafely() {
        try {
            refresh();
        } catch (RuntimeException exception) {
            log.warn("Failed to refresh Nacos prompts", exception);
        }
    }

    private void refresh() {
        for (Map.Entry<NacosPromptRequest, List<NacosPromptRegistry.NacosPromptTarget>> entry
                : registry.targets().entrySet()) {
            List<NacosPromptRegistry.NacosPromptTarget> targets = refreshTargets(entry.getValue());
            if (targets.isEmpty()) {
                continue;
            }

            refreshOne(entry.getKey(), targets);
        }
    }

    private void refreshOne(
            NacosPromptRequest request,
            List<NacosPromptRegistry.NacosPromptTarget> targets
    ) {
        try {
            NacosPromptValue value = client.getPrompt(request);
            if (!StringUtils.hasText(value.template())) {
                throw new IllegalStateException("Nacos prompt template is empty");
            }

            cache.put(request, value);
            statusRegistry.update(request, NacosPromptSource.REMOTE);
            snapshotStore.save(request, value);
            for (NacosPromptRegistry.NacosPromptTarget target : targets) {
                target.update(value.template());
            }
        } catch (RuntimeException exception) {
            statusRegistry.update(request, currentSource(request), exception);
            log.warn("Failed to refresh Nacos prompt: {}", request.snapshotKey(), exception);
        }
    }

    private NacosPromptSource currentSource(NacosPromptRequest request) {
        return statusRegistry.get(request)
                .map(NacosPromptStatus::source)
                .orElse(NacosPromptSource.EMPTY);
    }

    private List<NacosPromptRegistry.NacosPromptTarget> refreshTargets(List<NacosPromptRegistry.NacosPromptTarget> targets) {
        return targets.stream()
                .filter(target -> target.annotation().refresh())
                .toList();
    }
}
