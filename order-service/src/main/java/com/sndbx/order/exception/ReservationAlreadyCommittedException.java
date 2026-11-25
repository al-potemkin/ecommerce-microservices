package com.sndbx.order.exception;

import com.sndbx.order.exception.config.ErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

public class ReservationAlreadyCommittedException extends OrderServiceException {

    public ReservationAlreadyCommittedException(String detail) {
        super(ErrorCode.RESERVATION_ALREADY_COMMITTED, detail);
    }
}