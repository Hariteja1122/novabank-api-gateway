package com.novabank.gateway.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "novabank")
public record GatewayProperties(Services services, Jwt jwt, List<String> corsAllowedOrigins) {

    public record Services(@NotBlank String authUrl, @NotBlank String accountUrl, @NotBlank String transactionUrl) {
    }

    public record Jwt(@NotBlank @Size(min = 32) String secret, @NotBlank String issuer) {
    }
}
