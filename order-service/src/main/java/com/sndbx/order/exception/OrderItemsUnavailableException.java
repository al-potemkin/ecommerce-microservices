package com.sndbx.order.exception;

import com.sndbx.order.exception.config.ApiProblem;
import com.sndbx.order.exception.config.ErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

import java.util.List;

public class OrderItemsUnavailableException extends OrderServiceException {

    public OrderItemsUnavailableException(List<ApiProblem.FieldError> errors) {
        super(ErrorCode.ITEMS_UNAVAILABLE, "Some items are not available for purchase", errors);
    }
}