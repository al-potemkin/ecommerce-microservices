package com.sndbx.product.exception;

import com.sndbx.product.exception.config.ProductErrorCode;
import com.sndbx.product.exception.config.ProductServiceException;

public class NoActiveVariantsException extends ProductServiceException {
    public NoActiveVariantsException(String productId) {
        super(ProductErrorCode.NO_ACTIVE_VARIANTS, "Product '" + productId + "' has no active variants and cannot be activated");
    }
}