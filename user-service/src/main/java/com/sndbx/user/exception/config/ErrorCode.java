package com.sndbx.user.exception.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request", "The request is invalid"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required to access this resource"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource"),

    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "The requested resource was not found"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Address Not Found", "No address exists with this id"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "The HTTP method is not supported for this resource"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable", "The requested response format is not supported"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", "The request content type is not supported"),

    CONFLICT(HttpStatus.CONFLICT, "Conflict", "The request conflicts with the current state of the resource"),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "Concurrent Modification", "The resource was modified by another request, please retry"),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "The service is temporarily unavailable, please try again later");

    private final HttpStatus status;
    private final String title;
    private final String defaultDetail;

    public static ErrorCode fromStatus(int status) {
        for (ErrorCode code : values()) {
            if (code.status.value() == status) return code;
        }
        return status >= 500 ? INTERNAL_ERROR : BAD_REQUEST;
    }
}