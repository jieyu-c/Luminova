package cn.jieyuc.luminovanacos.prompt;

import cn.jieyuc.luminovanacos.annotation.NacosPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NacosPromptPreloadBeanFactoryPostProcessorTest {

    @Test
    void preloadDeduplicatesSamePromptRequest() {
        FakePromptClient client = new FakePromptClient(Map.of(
                request("demo.test"), new NacosPromptValue("remote prompt", "md5", "v1")
        ));

        GenericApplicationContext context = context(client, new NoopSnapshotStore());
        context.registerBean(FirstPromptBean.class);
        context.registerBean(SecondPromptBean.class);
        context.addBeanFactoryPostProcessor(new NacosPromptPreloadBeanFactoryPostProcessor());

        context.refresh();

        NacosPromptCache cache = context.getBean(NacosPromptCache.class);
        NacosPromptStatusRegistry statuses = context.getBean(NacosPromptStatusRegistry.class);
        assertThat(client.calls()).isEqualTo(1);
        assertThat(cache.get(request("demo.test")).map(NacosPromptValue::template)).contains("remote prompt");
        assertThat(statuses.get(request("demo.test")).map(NacosPromptStatus::source)).contains(NacosPromptSource.REMOTE);
        context.close();
    }

    @Test
    void preloadUsesSnapshotWhenRemoteFails() {
        NacosPromptRequest request = request("demo.snapshot");
        FakePromptClient client = new FakePromptClient(Map.of(), true);
        NoopSnapshotStore snapshotStore = new NoopSnapshotStore(Map.of(
                request, new NacosPromptValue("snapshot prompt", "md5", "v1")
        ));

        GenericApplicationContext context = context(client, snapshotStore);
        context.registerBean(SnapshotPromptBean.class);
        context.addBeanFactoryPostProcessor(new NacosPromptPreloadBeanFactoryPostProcessor());

        context.refresh();

        NacosPromptCache cache = context.getBean(NacosPromptCache.class);
        NacosPromptStatusRegistry statuses = context.getBean(NacosPromptStatusRegistry.class);
        assertThat(cache.get(request).map(NacosPromptValue::template)).contains("snapshot prompt");
        assertThat(statuses.get(request).map(NacosPromptStatus::source)).contains(NacosPromptSource.SNAPSHOT);
        assertThat(statuses.get(request).map(NacosPromptStatus::lastError)).hasValueSatisfying(error ->
                assertThat(error).contains("remote unavailable"));
        context.close();
    }

    @Test
    void preloadFailsOnNonStringPromptField() {
        FakePromptClient client = new FakePromptClient(Map.of());
        GenericApplicationContext context = context(client, new NoopSnapshotStore());
        context.registerBean(InvalidPromptBean.class);
        context.addBeanFactoryPostProcessor(new NacosPromptPreloadBeanFactoryPostProcessor());

        assertThatThrownBy(context::refresh)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("@NacosPrompt only supports String fields");
    }

    @Test
    void annotationProcessorInjectsPreloadedPromptBeforeDefaultValue() {
        NacosPromptCache cache = new NacosPromptCache();
        NacosPromptRegistry registry = new NacosPromptRegistry();
        NacosPromptStatusRegistry statusRegistry = new NacosPromptStatusRegistry();
        cache.put(request("demo.test"), new NacosPromptValue("remote prompt", "md5", "v1"));
        NacosPromptAnnotationBeanPostProcessor processor =
                new NacosPromptAnnotationBeanPostProcessor(cache, registry, statusRegistry);

        FirstPromptBean bean = new FirstPromptBean();
        processor.postProcessBeforeInitialization(bean, "firstPromptBean");

        assertThat(bean.prompt).isEqualTo("remote prompt");
    }

    @Test
    void preloadSkipsPerPromptCallsWhenAuthCheckFails() {
        AuthFailPromptClient client = new AuthFailPromptClient();
        GenericApplicationContext context = context(client, new NoopSnapshotStore());
        context.registerBean(FirstPromptBean.class);
        context.registerBean(SnapshotPromptBean.class);
        context.addBeanFactoryPostProcessor(new NacosPromptPreloadBeanFactoryPostProcessor());

        context.refresh();

        assertThat(client.calls()).isZero();
        assertThat(context.getBean(NacosPromptStatusRegistry.class).statuses())
                .hasSize(2)
                .allSatisfy((request, status) -> assertThat(status.source()).isEqualTo(NacosPromptSource.EMPTY));
        context.close();
    }

    private static GenericApplicationContext context(
            NacosPromptClient client,
            NacosPromptSnapshotStore snapshotStore
    ) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(NacosPromptCache.class);
        context.registerBean(NacosPromptStatusRegistry.class);
        context.registerBean(NacosPromptSnapshotStore.class, () -> snapshotStore);
        context.registerBean(NacosPromptClient.class, () -> client);
        return context;
    }

    private static NacosPromptRequest request(String key) {
        return new NacosPromptRequest(key, "", "");
    }

    static class FirstPromptBean {
        @NacosPrompt(key = "demo.test", defaultValue = "default")
        private String prompt;
    }

    static class SecondPromptBean {
        @NacosPrompt(key = "demo.test", defaultValue = "default")
        private String prompt;
    }

    static class SnapshotPromptBean {
        @NacosPrompt(key = "demo.snapshot", defaultValue = "default")
        private String prompt;
    }

    static class InvalidPromptBean {
        @NacosPrompt(key = "demo.invalid")
        private Integer prompt;
    }

    static class FakePromptClient extends NacosPromptClient {

        private final Map<NacosPromptRequest, NacosPromptValue> values;
        private final boolean fail;
        private final AtomicInteger calls = new AtomicInteger();

        FakePromptClient(Map<NacosPromptRequest, NacosPromptValue> values) {
            this(values, false);
        }

        FakePromptClient(Map<NacosPromptRequest, NacosPromptValue> values, boolean fail) {
            super(new MockEnvironment().withProperty("spring.cloud.nacos.server-addr", "127.0.0.1:8848"),
                    new ObjectMapper());
            this.values = values;
            this.fail = fail;
        }

        @Override
        public boolean canCallPromptApi(Duration timeout) {
            return true;
        }

        @Override
        public NacosPromptValue getPrompt(NacosPromptRequest promptRequest) {
            return getPrompt(promptRequest, Duration.ofSeconds(1));
        }

        @Override
        public NacosPromptValue getPrompt(NacosPromptRequest promptRequest, Duration timeout) {
            calls.incrementAndGet();
            if (fail) {
                throw new IllegalStateException("remote unavailable");
            }
            return values.getOrDefault(promptRequest, new NacosPromptValue("", "", ""));
        }

        int calls() {
            return calls.get();
        }
    }

    static class AuthFailPromptClient extends FakePromptClient {

        AuthFailPromptClient() {
            super(Map.of());
        }

        @Override
        public boolean canCallPromptApi(Duration timeout) {
            return false;
        }
    }

    static class NoopSnapshotStore extends NacosPromptSnapshotStore {

        private final Map<NacosPromptRequest, NacosPromptValue> snapshots;

        NoopSnapshotStore() {
            this(Map.of());
        }

        NoopSnapshotStore(Map<NacosPromptRequest, NacosPromptValue> snapshots) {
            super(new ObjectMapper());
            this.snapshots = snapshots;
        }

        @Override
        public Map<NacosPromptRequest, NacosPromptValue> load() {
            return snapshots;
        }

        @Override
        public void save(Map<NacosPromptRequest, NacosPromptValue> prompts) {
        }

        @Override
        public void save(NacosPromptRequest request, NacosPromptValue value) {
        }
    }
}
