package com.sndbx.inventory.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class TraceIdResolver {

    private final ObjectProvider<Tracer> tracer;

    public TraceIdResolver(ObjectProvider<Tracer> tracer) {
        this.tracer = tracer;
    }

    public String resolve() {
        Tracer t = tracer.getIfAvailable();
        Span span = t != null ? t.currentSpan() : null;
        return span != null ? span.context().traceId() : null;
    }
}