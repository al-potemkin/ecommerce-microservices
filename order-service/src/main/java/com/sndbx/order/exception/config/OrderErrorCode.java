package com.sndbx.order.exception.config;

import com.sndbx.common.exception.data.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order Not Found", "No order exists with this id"),

    INVALID_ORDER_TRANSITION(HttpStatus.CONFLICT, "Invalid Order Transition", "The order cannot move to the requested status"),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "Insufficient Stock", "Not enough stock for the requested items"),
    RESERVATION_RELEASED(HttpStatus.CONFLICT, "Reservation Released", "The stock reservation was released or expired"),
    RESERVATION_ALREADY_COMMITTED(HttpStatus.CONFLICT, "Reservation Already Committed", "The stock reservation is already committed"),
    ITEMS_UNAVAILABLE(HttpStatus.CONFLICT, "Items Unavailable", "Some items are not available for purchase"),
    RETURN_WINDOW_EXPIRED(HttpStatus.CONFLICT, "Return Window Expired", "The return period for this order has expired"),

    UPSTREAM_ERROR(HttpStatus.BAD_GATEWAY, "Upstream Error", "A dependent service returned an unexpected response");

    private final HttpStatus status;
    private final String title;
    private final String defaultDetail;
}