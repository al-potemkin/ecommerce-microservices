package com.sndbx.product.exception;

import com.sndbx.product.exception.config.ProductErrorCode;
import com.sndbx.product.exception.config.ProductServiceException;

public class ProductNotFoundException extends ProductServiceException {
    public ProductNotFoundException(String productId) {
        super(ProductErrorCode.PRODUCT_NOT_FOUND, "Product '" + productId + "' was not found");
    }
}