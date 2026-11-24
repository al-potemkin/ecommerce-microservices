package com.sndbx.order.exception;

import com.sndbx.order.exception.config.ErrorCode;
import com.sndbx.order.exception.config.OrderServiceException;

public class UpstreamErrorException extends OrderServiceException {

    public UpstreamErrorException(String dependency, int status) {
        super(ErrorCode.UPSTREAM_ERROR,
                "Service " + dependency + " responded with unexpected status " + status);
    }
}