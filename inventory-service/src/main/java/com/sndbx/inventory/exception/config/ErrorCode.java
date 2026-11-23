package com.sndbx.inventory.exception.config;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request", "The request is invalid"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required to access this resource"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource"),

    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "The requested resource was not found"),
    STOCK_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Stock Item Not Found", "No stock record exists for this SKU"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Reservation Not Found", "No reservation exists for this order"),

    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "Insufficient Stock", "Not enough stock to reserve the requested quantities"),
    RESERVATION_RELEASED(HttpStatus.CONFLICT, "Reservation Released", "The reservation was released or expired and cannot be committed"),
    RESERVATION_ALREADY_COMMITTED(HttpStatus.CONFLICT, "Reservation Already Committed", "The reservation is already committed and cannot be released"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "The HTTP method is not supported for this resource"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable", "The requested response format is not supported"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", "The request content type is not supported"),

    CONFLICT(HttpStatus.CONFLICT, "Conflict", "The request conflicts with the current state of the resource"),
    STOCK_BELOW_RESERVED(HttpStatus.CONFLICT, "Stock Below Reserved", "On-hand quantity cannot be lower than the reserved quantity"),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "Concurrent Modification", "The resource was modified by another request, please retry"),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "The service is temporarily unavailable, please try again later");

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

    public static ErrorCode fromStatus(int status) {
        for (ErrorCode code : values()) {
            if (code.status.value() == status) return code;
        }
        return status >= 500 ? INTERNAL_ERROR : BAD_REQUEST;
    }
}