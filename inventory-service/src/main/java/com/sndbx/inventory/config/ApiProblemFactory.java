package com.sndbx.inventory.config;

import com.sndbx.inventory.exception.config.ApiProblem;
import com.sndbx.inventory.exception.config.ErrorCode;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiProblemFactory {

    private final ObjectProvider<Tracer> tracer;
    private final String source;

    public ApiProblemFactory(ObjectProvider<Tracer> tracer,
                             @Value("${spring.application.name}") String source) {
        this.tracer = tracer;
        this.source = source;
    }

    public ApiProblem create(ErrorCode code, String detail, HttpServletRequest request,
                             List<ApiProblem.FieldError> errors) {
        return new ApiProblem(code,
                detail != null ? detail : code.getDefaultDetail(),
                request.getRequestURI(),
                currentTraceId(),
                source,
                errors);
    }

    private String currentTraceId() {
        Tracer t = tracer.getIfAvailable();
        Span span = t != null ? t.currentSpan() : null;
        return span != null ? span.context().traceId() : null;
    }
}