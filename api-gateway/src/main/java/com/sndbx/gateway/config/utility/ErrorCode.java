package com.sndbx.gateway.config.utility;

import org.springframework.http.HttpStatus;

import java.util.Locale;

public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request", "The request is invalid"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required to access this resource"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "The requested resource was not found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "The HTTP method is not supported for this resource"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", "Request rate limit exceeded, please try again later"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred"),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "Bad Gateway", "Invalid response from the upstream service"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "The service is temporarily unavailable, please try again later"),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Gateway Timeout", "The upstream service did not respond in time");

    private final HttpStatus status;
    private final String title;
    private final String defaultDetail;

    ErrorCode(HttpStatus status, String title, String defaultDetail) {
        this.status = status;
        this.title = title;
        this.defaultDetail = defaultDetail;
    }

    public HttpStatus status() {
        return status;
    }

    public String title() {
        return title;
    }

    public String defaultDetail() {
        return defaultDetail;
    }

    /**
     * unauthorized, service-unavailable... для поля type
     */
    public String slug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static ErrorCode fromStatus(int status) {
        for (ErrorCode code : values()) {
            if (code.status.value() == status) return code;
        }
        return status >= 500 ? INTERNAL_ERROR : BAD_REQUEST;
    }
}