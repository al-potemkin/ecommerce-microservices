package com.sndbx.order.exception;

import com.sndbx.order.exception.config.OrderErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

public class ReservationAlreadyCommittedException extends OrderServiceException {

    public ReservationAlreadyCommittedException(String detail) {
        super(OrderErrorCode.RESERVATION_ALREADY_COMMITTED, detail);
    }
}