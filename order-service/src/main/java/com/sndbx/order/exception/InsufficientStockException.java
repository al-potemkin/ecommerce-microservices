package com.sndbx.order.exception;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.order.exception.config.OrderErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

import java.util.List;

public class InsufficientStockException extends OrderServiceException {

    public InsufficientStockException(String detail, List<ApiProblem.FieldError> errors) {
        super(OrderErrorCode.INSUFFICIENT_STOCK, detail, errors);
    }
}