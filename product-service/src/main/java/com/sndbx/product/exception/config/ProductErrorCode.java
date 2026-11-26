package com.sndbx.product.exception.config;

import com.sndbx.common.exception.data.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    EMPTY_UPDATE(HttpStatus.BAD_REQUEST, "Empty Update", "At least one field must be provided"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product Not Found", "The requested product was not found"),
    VARIANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Variant Not Found", "The requested product variant was not found"),
    SKU_ALREADY_EXISTS(HttpStatus.CONFLICT, "SKU Already Exists", "A variant with this SKU already exists"),
    NO_ACTIVE_VARIANTS(HttpStatus.CONFLICT, "No Active Variants", "Product has no active variants and cannot be activated"),
    VARIANT_CONFLICT(HttpStatus.CONFLICT, "Variant Conflict", "Variants conflict with each other or with existing products");

    private final HttpStatus status;
    private final String title;
    private final String defaultDetail;
}