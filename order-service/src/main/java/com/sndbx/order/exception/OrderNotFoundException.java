package com.sndbx.order.exception;

import com.sndbx.order.exception.config.ErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

import java.util.UUID;

public class OrderNotFoundException extends OrderServiceException {

    public OrderNotFoundException(UUID orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, "Order " + orderId + " not found");
    }
}