package com.novabank.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Path based routing to the three backend services. /internal/** is deliberately not
 * routed, so service to service endpoints are never reachable from the internet.
 */
@Configuration
public class RoutesConfig {

    @Bean
    RouteLocator novabankRoutes(RouteLocatorBuilder builder, GatewayProperties props) {
        var s = props.services();
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**", "/api/admin/users/**")
                        .uri(s.authUrl()))
                .route("account-service", r -> r.path("/api/accounts/**", "/api/admin/accounts/**")
                        .uri(s.accountUrl()))
                .route("transaction-service", r -> r.path("/api/transactions/**", "/api/admin/transactions/**",
                                "/api/beneficiaries", "/api/beneficiaries/**")
                        .uri(s.transactionUrl()))
                .build();
    }
}
