package com.sndbx.gateway.controller;

import com.sndbx.gateway.config.utility.ErrorCode;
import com.sndbx.gateway.handler.ErrorResponder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    private final ErrorResponder errors;

    public FallbackController(ErrorResponder errors) {
        this.errors = errors;
    }

    @RequestMapping("/fallback/{service}")   // без метода: forward сохраняет GET/POST/...
    public Mono<Void> fallback(ServerWebExchange exchange, @PathVariable String service) {
        log.warn("Circuit breaker fallback for {}", service);
        exchange.getResponse().getHeaders().set(HttpHeaders.RETRY_AFTER, "10");
        return errors.write(exchange, ErrorCode.SERVICE_UNAVAILABLE);
    }
}