package com.sndbx.order.exception.config;

import com.sndbx.common.exception.ServiceException;
import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.common.exception.data.ErrorCode;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class OrderServiceException extends ServiceException {

    protected OrderServiceException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    protected OrderServiceException(ErrorCode errorCode, String detail,
                                    List<ApiProblem.FieldError> errors) {
        super(errorCode, detail, errors);
    }
}