package cn.jieyuc.luminovanacos.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class NacosPromptAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NacosPromptClient nacosPromptClient(
            Environment environment,
            ObjectMapper objectMapper
    ) {
        return new NacosPromptClient(environment, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosPromptCache nacosPromptCache() {
        return new NacosPromptCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosPromptStatusRegistry nacosPromptStatusRegistry() {
        return new NacosPromptStatusRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosPromptSnapshotStore nacosPromptSnapshotStore(ObjectMapper objectMapper) {
        return new NacosPromptSnapshotStore(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public static NacosPromptPreloadBeanFactoryPostProcessor nacosPromptPreloadBeanFactoryPostProcessor() {
        return new NacosPromptPreloadBeanFactoryPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosPromptRegistry nacosPromptRegistry() {
        return new NacosPromptRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosPromptAnnotationBeanPostProcessor nacosPromptAnnotationBeanPostProcessor(
            NacosPromptCache cache,
            NacosPromptRegistry registry,
            NacosPromptStatusRegistry statusRegistry
    ) {
        return new NacosPromptAnnotationBeanPostProcessor(cache, registry, statusRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosPromptRefresher nacosPromptRefresher(
            NacosPromptClient client,
            NacosPromptCache cache,
            NacosPromptRegistry registry,
            NacosPromptSnapshotStore snapshotStore,
            NacosPromptStatusRegistry statusRegistry
    ) {
        return new NacosPromptRefresher(client, cache, registry, snapshotStore, statusRegistry);
    }
}
