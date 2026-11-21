package com.sndbx.product.config;

import com.sndbx.product.config.utility.ApiProblem;
import com.sndbx.product.config.utility.ErrorCode;
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

    public ApiProblem create(ErrorCode code, HttpServletRequest request) {
        return create(code, null, request, null);
    }
}