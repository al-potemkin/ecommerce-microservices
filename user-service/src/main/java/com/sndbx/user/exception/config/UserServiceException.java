package com.sndbx.user.exception.config;

import com.sndbx.common.exception.ServiceException;
import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.common.exception.data.ErrorCode;

import java.util.List;

public abstract class UserServiceException extends ServiceException {

    protected UserServiceException(ErrorCode errorCode, String message, List<ApiProblem.FieldError> errors) {
        super(errorCode, message, errors);
    }

    protected UserServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}