package com.sndbx.product.exception;

import com.sndbx.product.config.utility.ErrorCode;

public class VariantNotFoundException extends ProductServiceException {
    public VariantNotFoundException(String productId, String sku) {
        super(ErrorCode.VARIANT_NOT_FOUND,
                "Variant with SKU '" + sku + "' was not found in product '" + productId + "'");
    }

    public VariantNotFoundException(String sku) {
        super(ErrorCode.VARIANT_NOT_FOUND, "Variant with SKU '" + sku + "' was not found");
    }
}