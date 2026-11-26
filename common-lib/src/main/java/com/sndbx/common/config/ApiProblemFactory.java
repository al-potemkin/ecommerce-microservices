package com.sndbx.common.config;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.common.exception.data.ErrorCode;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

public class ApiProblemFactory {

    private final ObjectProvider<Tracer> tracer;
    private final String source;

    public ApiProblemFactory(ObjectProvider<Tracer> tracer, String source) {
        this.tracer = tracer;
        this.source = source;
    }

    public ApiProblem create(ErrorCode code, String detail, HttpServletRequest request,
                             List<ApiProblem.FieldError> errors) {
        return new ApiProblem(
                code,
                detail != null ? detail : code.getDefaultDetail(),
                request.getRequestURI(),
                currentTraceId(),
                source,
                errors
        );
    }

    private String currentTraceId() {
        Tracer t = tracer.getIfAvailable();
        Span span = t != null ? t.currentSpan() : null;
        return span != null
                ? span.context().traceId()
                : null;
    }
}