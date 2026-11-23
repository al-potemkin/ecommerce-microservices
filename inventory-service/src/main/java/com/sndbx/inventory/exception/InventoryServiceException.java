package com.sndbx.inventory.exception.config;

import java.util.List;

public abstract class InventoryServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<ApiProblem.FieldError> errors;

    protected InventoryServiceException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, List.of());
    }

    protected InventoryServiceException(ErrorCode errorCode, String detail,
                                        List<ApiProblem.FieldError> errors) {
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