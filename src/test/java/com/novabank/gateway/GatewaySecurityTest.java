package com.novabank.gateway;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

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

    @Test
    void auditLogIsAdminOnly() throws Exception {
        for (String log : new String[]{"auth", "accounts", "transactions"}) {
            web.get().uri("/api/admin/audit/" + log).exchange().expectStatus().isUnauthorized();
            web.get().uri("/api/admin/audit/" + log).header("Authorization", "Bearer " + token("CUSTOMER"))
                    .exchange().expectStatus().isForbidden();
        }
    }

    private static String token(String role) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("novabank-auth").subject("3").claim("role", role)
                .issueTime(new Date()).expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner("test-secret-test-secret-test-secret-1234".getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }
}
