package com.sndbx.inventory.exception;

import com.sndbx.inventory.exception.config.ErrorCode;
import com.sndbx.inventory.exception.config.InventoryServiceException;

public class ReservationNotFoundException extends InventoryServiceException {

    public ReservationNotFoundException(String orderId) {
        super(ErrorCode.RESERVATION_NOT_FOUND, "No reservation found for order: " + orderId);
    }
}
