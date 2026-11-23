package com.sndbx.inventory.exception;

import com.sndbx.inventory.exception.config.ApiProblem;
import com.sndbx.inventory.exception.config.ErrorCode;
import com.sndbx.inventory.exception.config.InventoryServiceException;

import java.util.List;

public class InsufficientStockException extends InventoryServiceException {

    public InsufficientStockException(List<ApiProblem.FieldError> shortages) {
        super(ErrorCode.INSUFFICIENT_STOCK,
                "Not enough stock for " + shortages.size() + " item(s)", shortages);
    }
}