package com.novabank.gateway.health;

import com.novabank.gateway.config.GatewayProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Admin "System status" page: probes every backend's health endpoint in parallel. */
@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    private final WebClient client;
    private final GatewayProperties props;

    public SystemHealthController(WebClient.Builder builder, GatewayProperties props) {
        this.client = builder.build();
        this.props = props;
    }

    public record ServiceHealth(String service, String status, long latencyMs) {
    }

    public record SystemHealth(String overall, Instant checkedAt, List<ServiceHealth> services) {
    }

    @GetMapping("/health")
    public Mono<SystemHealth> health() {
        var s = props.services();
        Map<String, String> targets = Map.of(
                "auth-service", s.authUrl(),
                "account-service", s.accountUrl(),
                "transaction-service", s.transactionUrl());

        return Flux.fromIterable(targets.entrySet())
                .flatMap(e -> probe(e.getKey(), e.getValue()))
                .sort((a, b) -> a.service().compareTo(b.service()))
                .collectList()
                .map(list -> new SystemHealth(
                        list.stream().allMatch(h -> "UP".equals(h.status())) ? "UP" : "DEGRADED",
                        Instant.now(), list));
    }

    private Mono<ServiceHealth> probe(String name, String baseUrl) {
        long start = System.nanoTime();
        return client.get().uri(baseUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> String.valueOf(body.getOrDefault("status", "UNKNOWN")))
                .timeout(Duration.ofSeconds(4))
                .onErrorReturn("DOWN")
                .map(status -> new ServiceHealth(name, status, (System.nanoTime() - start) / 1_000_000));
    }
}
