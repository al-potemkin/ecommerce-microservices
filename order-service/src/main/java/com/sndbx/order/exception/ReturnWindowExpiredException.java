package com.sndbx.order.exception;

import com.sndbx.order.exception.config.OrderErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

public class ReturnWindowExpiredException extends OrderServiceException {

    public ReturnWindowExpiredException(int days) {
        super(OrderErrorCode.RETURN_WINDOW_EXPIRED, "Return is possible within " + days + " days after shipping");
    }
}
