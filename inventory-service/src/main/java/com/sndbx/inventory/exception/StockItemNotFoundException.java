package com.sndbx.inventory.exception;

import com.sndbx.inventory.exception.config.ApiProblem;
import com.sndbx.inventory.exception.config.ErrorCode;
import com.sndbx.inventory.exception.config.InventoryServiceException;

import java.util.Collection;

public class StockItemNotFoundException extends InventoryServiceException {

    public StockItemNotFoundException(String sku) {
        super(ErrorCode.STOCK_ITEM_NOT_FOUND, "Stock item not found for SKU: " + sku);
    }

    public StockItemNotFoundException(Collection<String> skus) {
        super(ErrorCode.STOCK_ITEM_NOT_FOUND,
                "Stock items not found for " + skus.size() + " SKU(s)",
                skus.stream()
                        .map(sku -> new ApiProblem.FieldError(sku, "stock item not found"))
                        .toList());
    }
}