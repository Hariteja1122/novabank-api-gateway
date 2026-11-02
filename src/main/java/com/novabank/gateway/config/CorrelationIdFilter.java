package com.novabank.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Stamps every routed request with X-Correlation-Id (or keeps the caller's), so one
 * user action can be traced across gateway, service and Db2 logs. Also logs timing.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
        String id = StringUtils.hasText(incoming) ? incoming : UUID.randomUUID().toString();
        long start = System.nanoTime();

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(h -> h.set(HEADER, id)))
                .build();
        mutated.getResponse().getHeaders().set(HEADER, id);

        return chain.filter(mutated).doFinally(signal -> log.info("{} {} -> {} in {} ms [{}]",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath().value(),
                mutated.getResponse().getStatusCode(),
                (System.nanoTime() - start) / 1_000_000,
                id));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
