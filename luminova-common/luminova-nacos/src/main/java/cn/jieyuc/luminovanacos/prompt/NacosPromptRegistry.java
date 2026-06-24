package cn.jieyuc.luminovanacos.prompt;

import cn.jieyuc.luminovanacos.annotation.NacosPrompt;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NacosPromptRegistry {

    private final Map<NacosPromptRequest, List<NacosPromptTarget>> targets = new ConcurrentHashMap<>();

    public void register(Object bean, Field field, NacosPrompt annotation) {
        validateField(field);
        NacosPromptRequest request = NacosPromptRequest.from(annotation);
        targets.computeIfAbsent(request, ignored -> new CopyOnWriteArrayList<>())
                .add(new NacosPromptTarget(bean, field, annotation));
    }

    public Map<NacosPromptRequest, List<NacosPromptTarget>> targets() {
        return targets;
    }

    private static void validateField(Field field) {
        if (!String.class.equals(field.getType())) {
            throw new IllegalStateException("@NacosPrompt only supports String fields: " + field);
        }
        if (Modifier.isFinal(field.getModifiers())) {
            throw new IllegalStateException("@NacosPrompt does not support final fields: " + field);
        }
    }

    public record NacosPromptTarget(
            Object bean,
            Field field,
            NacosPrompt annotation
    ) {

        public void update(String template) {
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, template);
        }
    }
}
