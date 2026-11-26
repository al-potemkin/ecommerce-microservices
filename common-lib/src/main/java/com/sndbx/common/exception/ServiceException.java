package com.sndbx.common.exception;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.common.exception.data.ErrorCode;

import java.util.List;

public abstract class ServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<ApiProblem.FieldError> errors;

    protected ServiceException(ErrorCode errorCode, String message) {
        this(errorCode, message, List.of());
    }

    protected ServiceException(ErrorCode errorCode, String message,
                               List<ApiProblem.FieldError> errors) {
        super(message, null, false, false);
        this.errorCode = errorCode;
        this.errors = errors != null ? errors : List.of();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<ApiProblem.FieldError> getErrors() {
        return errors;
    }
}