package com.sndbx.user.exception;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.common.exception.data.CommonErrorCode;
import com.sndbx.user.exception.config.UserServiceException;

import java.util.List;

public class InvalidAddressException extends UserServiceException {

    public InvalidAddressException(String detail, List<ApiProblem.FieldError> errors) {
        super(CommonErrorCode.VALIDATION_FAILED, detail, errors);
    }
}