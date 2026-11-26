package com.sndbx.order.exception;

import com.sndbx.order.exception.config.OrderErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

public class ReservationReleasedException extends OrderServiceException {

    public ReservationReleasedException(String detail) {
        super(OrderErrorCode.RESERVATION_RELEASED, detail);
    }
}