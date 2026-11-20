package com.sndbx.gateway.handler;

import com.sndbx.gateway.config.utility.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * before the standard Boot handler (-1)
 */
@Component
@Order(-2)
public class ApiErrorWebExceptionHandler implements WebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiErrorWebExceptionHandler.class);
    private final ErrorResponder errors;

    public ApiErrorWebExceptionHandler(ErrorResponder errors) {
        this.errors = errors;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        ErrorCode code = ex instanceof ResponseStatusException rse
                ? ErrorCode.fromStatus(rse.getStatusCode().value())
                : ErrorCode.INTERNAL_ERROR;

        if (code.status().is5xxServerError()) {
            log.error("{} {} failed", exchange.getRequest().getMethod(), exchange.getRequest().getPath(), ex);
        }
        return errors.write(exchange, code);
    }
}
