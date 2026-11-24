package com.sndbx.order.exception;

import com.sndbx.order.exception.config.ErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

public class DependencyUnavailableException extends OrderServiceException {

    public DependencyUnavailableException(String dependency) {
        super(ErrorCode.SERVICE_UNAVAILABLE, "Service " + dependency + " is temporarily unavailable");
    }
}