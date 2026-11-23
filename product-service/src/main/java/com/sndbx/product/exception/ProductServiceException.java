package com.sndbx.product.exception;

import com.sndbx.product.exception.config.ApiProblem;
import com.sndbx.product.exception.config.ErrorCode;

import java.util.List;

public abstract class ProductServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<ApiProblem.FieldError> errors;

    protected ProductServiceException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, List.of());
    }

    protected ProductServiceException(ErrorCode errorCode, String detail,
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