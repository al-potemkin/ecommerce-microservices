package com.sndbx.inventory.exception;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.inventory.exception.config.InventoryErrorCode;
import com.sndbx.inventory.exception.config.InventoryServiceException;

import java.util.List;

public class InsufficientStockException extends InventoryServiceException {

    public InsufficientStockException(List<ApiProblem.FieldError> shortages) {
        super(InventoryErrorCode.INSUFFICIENT_STOCK, "Not enough stock for " + shortages.size() + " item(s)", shortages);
    }
}