package com.sndbx.user.exception.config;

import lombok.Getter;

import java.util.List;

@Getter
public class UserServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<ApiProblem.FieldError> errors;

    public UserServiceException(ErrorCode errorCode) {
        this(errorCode, null, List.of());
    }

    public UserServiceException(ErrorCode errorCode, String message) {
        this(errorCode, message, List.of());
    }

    public UserServiceException(ErrorCode errorCode, String message, List<ApiProblem.FieldError> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors != null ? errors : List.of();
    }
}