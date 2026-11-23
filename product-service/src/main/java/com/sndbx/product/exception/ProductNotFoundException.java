package com.sndbx.product.exception;

import com.sndbx.product.exception.config.ErrorCode;

public class ProductNotFoundException extends ProductServiceException {
    public ProductNotFoundException(String productId) {
        super(ErrorCode.PRODUCT_NOT_FOUND, "Product '" + productId + "' was not found");
    }
}