package com.novabank.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Starts the gateway with dummy backend URLs and checks the edge security rules. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "novabank.services.auth-url=http://localhost:1",
        "novabank.services.account-url=http://localhost:1",
        "novabank.services.transaction-url=http://localhost:1",
        "novabank.jwt.secret=test-secret-test-secret-test-secret-1234",
        "novabank.cors-allowed-origins=http://localhost:4200"
})
@AutoConfigureWebTestClient
class GatewaySecurityTest {

    @Autowired
    WebTestClient web;

    @Test
    void protectedApiNeedsToken() {
        web.get().uri("/api/accounts").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void internalPathsAreNeverExposed() {
        web.get().uri("/internal/accounts?customerId=1").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void healthIsPublic() {
        web.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }
}
