package com.sndbx.inventory.exception.config;

import com.sndbx.common.exception.ServiceException;
import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.common.exception.data.ErrorCode;

import java.util.List;

public abstract class InventoryServiceException extends ServiceException {

    protected InventoryServiceException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    protected InventoryServiceException(ErrorCode errorCode, String detail,
                                        List<ApiProblem.FieldError> errors) {
        super(errorCode, detail, errors);
    }
}