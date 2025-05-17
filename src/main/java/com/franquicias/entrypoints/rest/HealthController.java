package com.franquicias.entrypoints.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HealthController {

    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> health() {
        return Mono.just("OK");
    }
}
