package com.sndbx.product.exception;

import com.sndbx.product.config.utility.ErrorCode;

public class NoActiveVariantsException extends ProductServiceException {
    public NoActiveVariantsException(String productId) {
        super(ErrorCode.NO_ACTIVE_VARIANTS,
                "Product '" + productId + "' has no active variants and cannot be activated");
    }
}