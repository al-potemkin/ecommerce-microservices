package com.sndbx.order.exception;

import com.sndbx.order.exception.config.ApiProblem;
import com.sndbx.order.exception.config.ErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

import java.util.List;

public class InsufficientStockException extends OrderServiceException {

    public InsufficientStockException(String detail, List<ApiProblem.FieldError> errors) {
        super(ErrorCode.INSUFFICIENT_STOCK, detail, errors);
    }
}