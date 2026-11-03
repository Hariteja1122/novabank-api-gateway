package com.novabank.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Path based routing to the three backend services. /internal/** is deliberately not
 * routed, so service to service endpoints are never reachable from the internet.
 * Each service keeps its own audit log, so /api/admin/audit/{auth|accounts|transactions}
 * goes to the service that owns that log.
 */
@Configuration
public class RoutesConfig {

    @Bean
    RouteLocator novabankRoutes(RouteLocatorBuilder builder, GatewayProperties props) {
        var s = props.services();
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**", "/api/admin/users/**", "/api/admin/audit/auth/**")
                        .uri(s.authUrl()))
                .route("account-service", r -> r.path("/api/accounts/**", "/api/admin/accounts/**",
                                "/api/admin/audit/accounts/**")
                        .uri(s.accountUrl()))
                .route("transaction-service", r -> r.path("/api/transactions/**", "/api/admin/transactions/**",
                                "/api/admin/audit/transactions/**",
                                "/api/beneficiaries", "/api/beneficiaries/**")
                        .uri(s.transactionUrl()))
                .build();
    }
}
