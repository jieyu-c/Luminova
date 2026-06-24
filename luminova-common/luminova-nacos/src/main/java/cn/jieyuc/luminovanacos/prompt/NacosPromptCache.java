package cn.jieyuc.luminovanacos.prompt;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NacosPromptCache {

    private final Map<NacosPromptRequest, NacosPromptValue> prompts = new ConcurrentHashMap<>();

    public Optional<NacosPromptValue> get(NacosPromptRequest request) {
        return Optional.ofNullable(prompts.get(request));
    }

    public NacosPromptValue put(NacosPromptRequest request, NacosPromptValue value) {
        prompts.put(request, value);
        return value;
    }

    public void putAll(Map<NacosPromptRequest, NacosPromptValue> values) {
        prompts.putAll(values);
    }

    public Map<NacosPromptRequest, NacosPromptValue> values() {
        return Map.copyOf(prompts);
    }
}
