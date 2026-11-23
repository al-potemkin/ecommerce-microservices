package com.sndbx.inventory.exception;

import com.sndbx.inventory.exception.config.ErrorCode;
import com.sndbx.inventory.exception.config.InventoryServiceException;

public class StockBelowReservedException extends InventoryServiceException {

    public StockBelowReservedException(String sku, int requestedOnHand, int reserved) {
        super(ErrorCode.STOCK_BELOW_RESERVED,
                "Cannot set on-hand to " + requestedOnHand + " for SKU " + sku
                        + ": " + reserved + " units are reserved");
    }
}