package com.sndbx.order.exception;

import com.sndbx.common.exception.data.CommonErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

public class DependencyUnavailableException extends OrderServiceException {

    public DependencyUnavailableException(String dependency) {
        super(CommonErrorCode.SERVICE_UNAVAILABLE, "Service " + dependency + " is temporarily unavailable");
    }
}