//package com.sndbx.product.exception.config;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import org.springframework.http.HttpStatus;
//
//@Getter
//@AllArgsConstructor
//public enum ErrorCode {
//    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request", "The request is invalid"),
//    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed"),
//    EMPTY_UPDATE(HttpStatus.BAD_REQUEST, "Empty Update", "At least one field must be provided"),
//
//    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required to access this resource"),
//    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource"),
//
//    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "The requested resource was not found"),
//    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product Not Found", "The requested product was not found"),
//    VARIANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Variant Not Found", "The requested product variant was not found"),
//
//    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "The HTTP method is not supported for this resource"),
//    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable", "The requested response format is not supported"),
//    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", "The request content type is not supported"),
//
//    CONFLICT(HttpStatus.CONFLICT, "Conflict", "The request conflicts with the current state of the resource"),
//    SKU_ALREADY_EXISTS(HttpStatus.CONFLICT, "SKU Already Exists", "A variant with this SKU already exists"),
//    NO_ACTIVE_VARIANTS(HttpStatus.CONFLICT, "No Active Variants", "Product has no active variants and cannot be activated"),
//    VARIANT_CONFLICT(HttpStatus.CONFLICT, "Variant Conflict", "Variants conflict with each other or with existing products"),
//    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "Concurrent Modification", "The resource was modified by another request, please retry"),
//
//    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred"),
//    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "The service is temporarily unavailable, please try again later");
//
//    private final HttpStatus status;
//    private final String title;
//    private final String defaultDetail;
//
//    public static ErrorCode fromStatus(int status) {
//        for (ErrorCode code : values()) {
//            if (code.status.value() == status) return code;
//        }
//        return status >= 500 ? INTERNAL_ERROR : BAD_REQUEST;
//    }
//}