package cn.jieyuc.luminovagateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 网关请求链路 ID 过滤器。
 *
 * <p>保留长度合法的上游请求 ID，否则生成新 UUID，并同时写入下游请求与响应。该值只用于
 * 链路追踪，不能携带用户身份或其他敏感数据。</p>
 */
@Component
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {
    private static final String HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
        String requestId = incoming == null || incoming.isBlank() || incoming.length() > 64
                ? UUID.randomUUID().toString() : incoming;
        ServerHttpRequest request = exchange.getRequest().mutate().headers(headers -> {
            headers.remove(HEADER);
            headers.add(HEADER, requestId);
        }).build();
        exchange.getResponse().getHeaders().set(HEADER, requestId);
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
