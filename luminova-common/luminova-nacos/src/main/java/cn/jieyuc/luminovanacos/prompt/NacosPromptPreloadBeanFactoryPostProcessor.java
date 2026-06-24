package cn.jieyuc.luminovanacos.prompt;

import cn.jieyuc.luminovanacos.annotation.NacosPrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class NacosPromptPreloadBeanFactoryPostProcessor implements BeanFactoryPostProcessor,
        PriorityOrdered, BeanClassLoaderAware {

    private static final Logger log = LoggerFactory.getLogger(NacosPromptPreloadBeanFactoryPostProcessor.class);
    private static final int MAX_PARALLELISM = 8;

    private ClassLoader beanClassLoader;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        long startedAt = System.nanoTime();
        List<NacosPromptDefinition> definitions = scanDefinitions(beanFactory);
        if (definitions.isEmpty()) {
            log.info("Nacos prompt preload skipped: no @NacosPrompt fields found");
            return;
        }

        NacosPromptCache cache = beanFactory.getBean(NacosPromptCache.class);
        NacosPromptStatusRegistry statusRegistry = beanFactory.getBean(NacosPromptStatusRegistry.class);
        NacosPromptSnapshotStore snapshotStore = beanFactory.getBean(NacosPromptSnapshotStore.class);
        NacosPromptClient client = beanFactory.getBean(NacosPromptClient.class);

        Map<NacosPromptRequest, NacosPromptValue> snapshots = snapshotStore.load();
        cache.putAll(snapshots);

        Set<NacosPromptRequest> requests = new LinkedHashSet<>();
        definitions.forEach(definition -> requests.add(definition.request()));
        PreloadSummary summary = preload(requests, snapshots, client, cache, statusRegistry);

        snapshotStore.save(cache.values());
        summary.applyFallbackCounts(definitions, snapshots, cache);
        logSummary(summary, elapsedMillis(startedAt));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    private List<NacosPromptDefinition> scanDefinitions(ConfigurableListableBeanFactory beanFactory) {
        List<NacosPromptDefinition> definitions = new ArrayList<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            Class<?> beanClass = resolveBeanClass(beanDefinition);
            if (beanClass == null) {
                continue;
            }
            ReflectionUtils.doWithFields(beanClass, field -> addDefinition(definitions, beanName, field));
        }
        return definitions;
    }

    private void addDefinition(List<NacosPromptDefinition> definitions, String beanName, Field field) {
        NacosPrompt annotation = field.getAnnotation(NacosPrompt.class);
        if (annotation == null) {
            return;
        }
        validateAnnotation(annotation);
        validateField(field);
        definitions.add(new NacosPromptDefinition(beanName, field, annotation, NacosPromptRequest.from(annotation)));
    }

    private PreloadSummary preload(
            Set<NacosPromptRequest> requests,
            Map<NacosPromptRequest, NacosPromptValue> snapshots,
            NacosPromptClient client,
            NacosPromptCache cache,
            NacosPromptStatusRegistry statusRegistry
    ) {
        ExecutorService executorService = Executors.newFixedThreadPool(
                Math.min(requests.size(), MAX_PARALLELISM),
                runnable -> {
                    Thread thread = new Thread(runnable, "nacos-prompt-preload");
                    thread.setDaemon(true);
                    return thread;
                }
        );

        PreloadSummary summary = new PreloadSummary(requests.size());
        Duration timeout = client.preloadTimeout();
        if (!client.canCallPromptApi(timeout)) {
            IllegalStateException exception = new IllegalStateException(
                    "Nacos prompt preload auth check failed within " + timeout.toMillis() + "ms"
            );
            requests.forEach(request -> fallback(request, snapshots, statusRegistry, summary, exception));
            return summary;
        }

        try {
            List<CompletableFuture<Void>> futures = requests.stream()
                    .map(request -> CompletableFuture.runAsync(
                            () -> fetchPrompt(request, snapshots, client, cache, statusRegistry, summary, timeout),
                            executorService
                    ))
                    .toList();
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        } finally {
            executorService.shutdownNow();
        }
        return summary;
    }

    private void fetchPrompt(
            NacosPromptRequest request,
            Map<NacosPromptRequest, NacosPromptValue> snapshots,
            NacosPromptClient client,
            NacosPromptCache cache,
            NacosPromptStatusRegistry statusRegistry,
            PreloadSummary summary,
            Duration timeout
    ) {
        try {
            NacosPromptValue value = client.getPrompt(request, timeout);
            if (StringUtils.hasText(value.template())) {
                cache.put(request, value);
                statusRegistry.update(request, NacosPromptSource.REMOTE);
                summary.remoteSuccess.incrementAndGet();
                return;
            }
            throw new IllegalStateException("Nacos prompt template is empty");
        } catch (RuntimeException exception) {
            fallback(request, snapshots, statusRegistry, summary, exception);
        }
    }

    private void fallback(
            NacosPromptRequest request,
            Map<NacosPromptRequest, NacosPromptValue> snapshots,
            NacosPromptStatusRegistry statusRegistry,
            PreloadSummary summary,
            RuntimeException exception
    ) {
        summary.failures.add(request.snapshotKey());
        if (snapshots.containsKey(request)) {
            statusRegistry.update(request, NacosPromptSource.SNAPSHOT, exception);
            summary.snapshotFallback.incrementAndGet();
            return;
        }
        statusRegistry.update(request, NacosPromptSource.EMPTY, exception);
    }

    private Class<?> resolveBeanClass(BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getResolvableType().resolve();
        if (beanClass != null) {
            return beanClass;
        }

        String className = beanDefinition.getBeanClassName();
        if (!StringUtils.hasText(className)) {
            return null;
        }
        return resolveClass(className);
    }

    private Class<?> resolveClass(String className) {
        try {
            return ClassUtils.forName(className, beanClassLoader);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void validateAnnotation(NacosPrompt annotation) {
        if (!StringUtils.hasText(annotation.key())) {
            throw new IllegalStateException("@NacosPrompt key must not be empty");
        }
    }

    private static void validateField(Field field) {
        if (!String.class.equals(field.getType())) {
            throw new IllegalStateException("@NacosPrompt only supports String fields: " + field);
        }
        if (Modifier.isFinal(field.getModifiers())) {
            throw new IllegalStateException("@NacosPrompt does not support final fields: " + field);
        }
    }

    private void logSummary(PreloadSummary summary, long elapsedMillis) {
        if (summary.failures.isEmpty()) {
            log.info("Nacos prompt preload finished: total={}, remote={}, snapshot={}, default={}, empty={}, elapsedMs={}",
                    summary.total,
                    summary.remoteSuccess.get(),
                    summary.snapshotFallback.get(),
                    summary.defaultFallback.get(),
                    summary.emptyFallback.get(),
                    elapsedMillis);
            return;
        }
        log.warn("Nacos prompt preload finished with fallback: total={}, remote={}, snapshot={}, default={}, empty={}, elapsedMs={}, failedKeys={}",
                summary.total,
                summary.remoteSuccess.get(),
                summary.snapshotFallback.get(),
                summary.defaultFallback.get(),
                summary.emptyFallback.get(),
                elapsedMillis,
                summary.failures);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private record NacosPromptDefinition(
            String beanName,
            Field field,
            NacosPrompt annotation,
            NacosPromptRequest request
    ) {
    }

    private static class PreloadSummary {

        private final int total;
        private final AtomicInteger remoteSuccess = new AtomicInteger();
        private final AtomicInteger snapshotFallback = new AtomicInteger();
        private final AtomicInteger defaultFallback = new AtomicInteger();
        private final AtomicInteger emptyFallback = new AtomicInteger();
        private final List<String> failures = new CopyOnWriteArrayList<>();

        private PreloadSummary(int total) {
            this.total = total;
        }

        private void applyFallbackCounts(
                List<NacosPromptDefinition> definitions,
                Map<NacosPromptRequest, NacosPromptValue> snapshots,
                NacosPromptCache cache
        ) {
            Set<NacosPromptRequest> missing = new LinkedHashSet<>();
            definitions.forEach(definition -> {
                if (cache.get(definition.request()).isEmpty() && !snapshots.containsKey(definition.request())) {
                    missing.add(definition.request());
                }
            });
            for (NacosPromptRequest request : missing) {
                boolean hasDefault = definitions.stream()
                        .filter(definition -> definition.request().equals(request))
                        .anyMatch(definition -> StringUtils.hasText(definition.annotation().defaultValue()));
                if (hasDefault) {
                    defaultFallback.incrementAndGet();
                } else {
                    emptyFallback.incrementAndGet();
                }
            }
        }
    }
}
