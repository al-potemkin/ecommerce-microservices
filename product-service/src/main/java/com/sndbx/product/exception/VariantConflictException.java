package com.sndbx.product.exception;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.product.exception.config.ProductErrorCode;
import com.sndbx.product.exception.config.ProductServiceException;

import java.util.List;

public class VariantConflictException extends ProductServiceException {

    public VariantConflictException(List<ApiProblem.FieldError> errors) {
        super(ProductErrorCode.VARIANT_CONFLICT, "Variants contain duplicate or already used SKU and volume values", errors);
    }
}