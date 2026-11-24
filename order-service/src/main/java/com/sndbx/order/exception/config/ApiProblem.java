package com.sndbx.order.exception.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sndbx.order.exception.config.ErrorCode;
import lombok.Getter;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Getter
@JsonPropertyOrder({
        "title", "status", "detail", "instance",
        "code", "timestamp", "traceId", "source", "errors"
})
public class ApiProblem extends ProblemDetail {

    private final String code;
    private final String timestamp;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private final String traceId;
    private final String source;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private final List<FieldError> errors;

    public ApiProblem(ErrorCode errorCode, String detail, String instance,
                      String traceId, String source, List<FieldError> errors) {
        super(errorCode.getStatus().value());
        setTitle(errorCode.getTitle());
        setDetail(detail);
        setInstance(toUri(instance));
        this.code = errorCode.name();
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        this.traceId = traceId;
        this.source = source;
        this.errors = errors != null ? errors : List.of();
    }

    public record FieldError(String field, String message) {
    }

    private static URI toUri(String path) {
        try {
            return URI.create(path);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}