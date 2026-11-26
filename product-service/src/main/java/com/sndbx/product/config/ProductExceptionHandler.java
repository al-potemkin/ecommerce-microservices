package com.sndbx.product.config;

import com.sndbx.common.config.ApiProblemFactory;
import com.sndbx.common.exception.GlobalExceptionHandler;
import com.sndbx.product.exception.config.ProductErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductExceptionHandler extends GlobalExceptionHandler {

    public ProductExceptionHandler(ApiProblemFactory problems) {
        super(problems);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<Object> handleDuplicateKey(DuplicateKeyException ex, HttpServletRequest request) {
        return respond(ProductErrorCode.SKU_ALREADY_EXISTS, ex, request);
    }
}