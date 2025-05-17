package com.franquicias.entrypoints.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@WebFluxTest(controllers = HealthController.class)
class HealthControllerTest {

    @Autowired
    private WebTestClient client;

    @Test
    void health_returnsOk() {
        client.get().uri("/health")
              .exchange()
              .expectStatus().isOk()
              .expectBody(String.class).isEqualTo("OK");
    }

    @Test
    void health_isMono() {
        client.get().uri("/health")
              .exchange()
              .returnResult(String.class)
              .getResponseBody()
              .as(StepVerifier::create)
              .expectNext("OK")
              .verifyComplete();
    }
}
