package com.sndbx.product.exception;

import com.sndbx.product.config.utility.ErrorCode;
import org.springframework.http.HttpStatus;

public class EmptyUpdateException extends ProductServiceException {
    public EmptyUpdateException() {
        super(ErrorCode.EMPTY_UPDATE, "At least one field (price or active) must be provided");
    }
}