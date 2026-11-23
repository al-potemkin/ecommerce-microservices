package com.sndbx.inventory.config;

import com.sndbx.inventory.exception.config.ApiProblem;
import com.sndbx.inventory.exception.config.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProblemFactory {

    private final String source;
    private final TraceIdResolver traceIdResolver;

    public ProblemFactory(@Value("${spring.application.name}") String source,
                          TraceIdResolver traceIdResolver) {
        this.source = source;
        this.traceIdResolver = traceIdResolver;
    }

    public ApiProblem create(ErrorCode code, String detail, HttpServletRequest request,
                             List<ApiProblem.FieldError> errors) {
        return new ApiProblem(code,
                detail != null ? detail : code.defaultDetail(),
                request.getRequestURI(),
                traceIdResolver.resolve(),
                source,
                errors);
    }
}