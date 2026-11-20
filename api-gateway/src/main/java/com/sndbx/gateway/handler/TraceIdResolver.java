package com.sndbx.gateway.handler;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingObservationHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class TraceIdResolver {

    private final ObjectProvider<Tracer> tracer;

    public TraceIdResolver(ObjectProvider<Tracer> tracer) {
        this.tracer = tracer;
    }

    public String resolve(ServerWebExchange exchange) {
        String fromObservation = ServerRequestObservationContext.findCurrent(exchange.getAttributes())
                .map(ctx -> ctx.<TracingObservationHandler.TracingContext>get(
                        TracingObservationHandler.TracingContext.class))
                .map(TracingObservationHandler.TracingContext::getSpan)
                .map(span -> span.context().traceId())
                .orElse(null);
        if (fromObservation != null) {
            return fromObservation;
        }

        Tracer t = tracer.getIfAvailable();
        Span span = t != null ? t.currentSpan() : null;
        return span != null ? span.context().traceId() : null;
    }
}
