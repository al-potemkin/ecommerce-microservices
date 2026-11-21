package com.sndbx.product.exception;

import com.sndbx.product.config.utility.ApiProblem;
import com.sndbx.product.config.utility.ErrorCode;

import java.util.List;

public class VariantConflictException extends ProductServiceException {

    public VariantConflictException(List<ApiProblem.FieldError> errors) {
        super(ErrorCode.VARIANT_CONFLICT,
                "Variants contain duplicate or already used SKU and volume values", errors);
    }
}