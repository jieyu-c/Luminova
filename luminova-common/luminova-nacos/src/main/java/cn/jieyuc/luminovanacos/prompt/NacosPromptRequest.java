package cn.jieyuc.luminovanacos.prompt;

import cn.jieyuc.luminovanacos.annotation.NacosPrompt;

import java.util.Objects;

public record NacosPromptRequest(
        String key,
        String version,
        String label
) {

    public static NacosPromptRequest from(NacosPrompt annotation) {
        return new NacosPromptRequest(
                annotation.key(),
                annotation.version(),
                annotation.label()
        );
    }

    public NacosPromptRequest {
        Objects.requireNonNull(key, "key must not be null");
        version = version == null ? "" : version;
        label = label == null ? "" : label;
    }

    public String snapshotKey() {
        return key + "|" + version + "|" + label;
    }
}
