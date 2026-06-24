package cn.jieyuc.luminovanacos.prompt;

import java.time.Instant;

public record NacosPromptStatus(
        NacosPromptRequest request,
        NacosPromptSource source,
        Instant lastUpdatedAt,
        String lastError
) {
}
