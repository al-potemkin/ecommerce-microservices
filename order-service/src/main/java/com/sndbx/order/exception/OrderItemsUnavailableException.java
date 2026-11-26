package com.sndbx.order.exception;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.order.exception.config.OrderErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

import java.util.List;

public class OrderItemsUnavailableException extends OrderServiceException {

    public OrderItemsUnavailableException(List<ApiProblem.FieldError> errors) {
        super(OrderErrorCode.ITEMS_UNAVAILABLE, "Some items are not available for purchase", errors);
    }
}