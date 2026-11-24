package com.sndbx.order.exception.config;

import java.util.List;

public abstract class OrderServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<ApiProblem.FieldError> errors;

    protected OrderServiceException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, List.of());
    }

    protected OrderServiceException(ErrorCode errorCode, String detail, List<ApiProblem.FieldError> errors) {
        super(detail, null, false, false);
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<ApiProblem.FieldError> getErrors() {
        return errors;
    }
}