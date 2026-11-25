package com.sndbx.order.exception;

import com.sndbx.order.exception.config.ErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

public class ReservationReleasedException extends OrderServiceException {

    public ReservationReleasedException(String detail) {
        super(ErrorCode.RESERVATION_RELEASED, detail);
    }
}