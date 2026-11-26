package com.sndbx.inventory.config;

import com.sndbx.common.config.ApiProblemFactory;
import com.sndbx.common.exception.GlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InventoryExceptionHandler extends GlobalExceptionHandler {

    public InventoryExceptionHandler(ApiProblemFactory problems) {
        super(problems);
    }
}