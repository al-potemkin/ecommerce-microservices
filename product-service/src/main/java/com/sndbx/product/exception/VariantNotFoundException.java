package com.sndbx.product.exception;

import com.sndbx.product.exception.config.ProductErrorCode;
import com.sndbx.product.exception.config.ProductServiceException;

public class VariantNotFoundException extends ProductServiceException {
    public VariantNotFoundException(String productId, String sku) {
        super(ProductErrorCode.VARIANT_NOT_FOUND, "Variant with SKU '" + sku + "' was not found in product '" + productId + "'");
    }

    public VariantNotFoundException(String sku) {
        super(ProductErrorCode.VARIANT_NOT_FOUND, "Variant with SKU '" + sku + "' was not found");
    }
}