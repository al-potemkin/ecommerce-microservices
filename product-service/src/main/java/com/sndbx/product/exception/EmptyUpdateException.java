package com.sndbx.product.exception;

import com.sndbx.product.exception.config.ProductErrorCode;
import com.sndbx.product.exception.config.ProductServiceException;

public class EmptyUpdateException extends ProductServiceException {
    public EmptyUpdateException() {
        super(ProductErrorCode.EMPTY_UPDATE, "At least one field (price or active) must be provided");
    }
}