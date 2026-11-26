package com.sndbx.inventory.exception.config;

import com.sndbx.common.exception.data.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InventoryErrorCode implements ErrorCode {
    STOCK_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Stock Item Not Found", "No stock record exists for this SKU"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Reservation Not Found", "No reservation exists for this order"),

    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "Insufficient Stock", "Not enough stock to reserve the requested quantities"),
    RESERVATION_RELEASED(HttpStatus.CONFLICT, "Reservation Released", "The reservation was released or expired and cannot be committed"),
    RESERVATION_ALREADY_COMMITTED(HttpStatus.CONFLICT, "Reservation Already Committed", "The reservation is already committed and cannot be released"),
    STOCK_BELOW_RESERVED(HttpStatus.CONFLICT, "Stock Below Reserved", "On-hand quantity cannot be lower than the reserved quantity");

    private final HttpStatus status;
    private final String title;
    private final String defaultDetail;
}
