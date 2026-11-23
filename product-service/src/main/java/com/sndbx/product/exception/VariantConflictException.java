package com.sndbx.product.exception;

import com.sndbx.product.exception.config.ApiProblem;
import com.sndbx.product.exception.config.ErrorCode;

import java.util.List;

public class VariantConflictException extends ProductServiceException {

    public VariantConflictException(List<ApiProblem.FieldError> errors) {
        super(ErrorCode.VARIANT_CONFLICT,
                "Variants contain duplicate or already used SKU and volume values", errors);
    }
}