package com.sndbx.order.exception;

import com.sndbx.order.entity.OrderStatus;
import com.sndbx.order.exception.config.ErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

import java.util.UUID;

public class InvalidOrderTransitionException extends OrderServiceException {

    public InvalidOrderTransitionException(UUID orderId, OrderStatus from, OrderStatus to) {
        super(ErrorCode.INVALID_ORDER_TRANSITION,
                "Order " + orderId + " cannot move from " + from + " to " + to);
    }
}