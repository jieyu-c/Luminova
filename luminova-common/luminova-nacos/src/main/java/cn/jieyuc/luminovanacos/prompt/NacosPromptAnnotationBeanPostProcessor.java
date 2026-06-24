package cn.jieyuc.luminovanacos.prompt;

import cn.jieyuc.luminovanacos.annotation.NacosPrompt;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

public class NacosPromptAnnotationBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    private final NacosPromptCache cache;
    private final NacosPromptRegistry registry;
    private final NacosPromptStatusRegistry statusRegistry;

    public NacosPromptAnnotationBeanPostProcessor(
            NacosPromptCache cache,
            NacosPromptRegistry registry,
            NacosPromptStatusRegistry statusRegistry
    ) {
        this.cache = cache;
        this.registry = registry;
        this.statusRegistry = statusRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> inject(bean, field));
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private void inject(Object bean, Field field) {
        NacosPrompt annotation = field.getAnnotation(NacosPrompt.class);
        if (annotation == null) {
            return;
        }

        validateAnnotation(annotation);
        String template = resolveTemplate(annotation);

        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, bean, template);
        registry.register(bean, field, annotation);
    }

    private String resolveTemplate(NacosPrompt annotation) {
        NacosPromptRequest request = NacosPromptRequest.from(annotation);
        NacosPromptValue value = cache.get(request).orElse(null);
        if (value != null && StringUtils.hasText(value.template())) {
            return value.template();
        }

        if (StringUtils.hasText(annotation.defaultValue())) {
            statusRegistry.updateFallback(request, NacosPromptSource.DEFAULT);
            return annotation.defaultValue();
        }

        statusRegistry.updateFallback(request, NacosPromptSource.EMPTY);
        return "";
    }

    private static void validateAnnotation(NacosPrompt annotation) {
        if (!StringUtils.hasText(annotation.key())) {
            throw new IllegalStateException("@NacosPrompt key must not be empty");
        }
    }
}
