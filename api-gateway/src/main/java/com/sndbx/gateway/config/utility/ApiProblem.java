package com.sndbx.gateway.config.utility;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@JsonPropertyOrder({
        "title", "status", "detail", "instance",
        "code", "timestamp", "traceId", "source", "errors"
})
public class ApiProblem extends ProblemDetail {

    private final String code;
    private final String timestamp;
    private final String traceId;
    private final String source;
    private final List<FieldError> errors;

    public ApiProblem(ErrorCode errorCode, String detail,
                      String instance, String traceId, String source,
                      List<FieldError> errors) {
        super(errorCode.status().value());
        setTitle(errorCode.title());
        setDetail(detail);
        setInstance(toUri(instance));
        this.code = errorCode.name();
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        this.traceId = traceId;
        this.source = source;
        this.errors = errors != null ? errors : List.of();
    }

    public String getCode() {
        return code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getTraceId() {
        return traceId;
    }

    public String getSource() {
        return source;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public List<FieldError> getErrors() {
        return errors;
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