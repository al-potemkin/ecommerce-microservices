package com.sndbx.inventory.exception;

import com.sndbx.common.exception.data.ErrorCode;
import com.sndbx.inventory.exception.config.InventoryServiceException;

public class ReservationConflictException extends InventoryServiceException {

    public ReservationConflictException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}