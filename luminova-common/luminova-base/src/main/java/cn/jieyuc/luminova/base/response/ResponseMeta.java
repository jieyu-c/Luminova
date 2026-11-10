package cn.jieyuc.luminova.base.response;

import java.time.Instant;

/**
 * 响应关联元数据，用于客户端报障和服务端链路检索。
 *
 * @param requestId 当前 HTTP 请求标识
 * @param traceId 当前分布式追踪标识；未接入追踪时为 {@code unknown}
 * @param timestamp 服务端生成响应的 UTC 时间
 */
public record ResponseMeta(String requestId, String traceId, Instant timestamp) {
}
